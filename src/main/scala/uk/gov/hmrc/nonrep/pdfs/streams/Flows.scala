package uk.gov.hmrc.nonrep.pdfs
package streams

import java.text.{DecimalFormat, SimpleDateFormat}
import java.util.{Calendar, Date, UUID}

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.FlowShape
import akka.stream.alpakka.s3.headers.CannedAcl
import akka.stream.alpakka.s3.{ApiVersion, MetaHeaders, MultipartUploadResult, S3Attributes, S3Ext, S3Headers}
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Partition, Sink}
import akka.util.ByteString
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sts.StsClient
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest
import uk.gov.hmrc.nonrep.pdfs.model._
import uk.gov.hmrc.nonrep.pdfs.server.ServiceConfig
import uk.gov.hmrc.nonrep.pdfs.service._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object Flows {
  def apply()(implicit system: ActorSystem[_],
              config: ServiceConfig,
              apiKeyValidator: Validator[ApiKey],
              payloadValidator: Validator[PayloadWithSchema],
              connector: ServiceConnector[UnsignedPdfDocument],
              parser: ServiceResponse[SignedPdfDocument],
              template: PdfDocumentTemplate[AcceptedRequest],
              generator: PdfDocumentGenerator[ValidatedDocument],
              extender: PdfDocumentExtender[EitherNelErr[ValidatedDocument]]) = new Flows()
}

class Flows(implicit val system: ActorSystem[_],
            config: ServiceConfig,
            apiKeyValidator: Validator[ApiKey],
            payloadValidator: Validator[PayloadWithSchema],
            connector: ServiceConnector[UnsignedPdfDocument],
            parser: ServiceResponse[SignedPdfDocument],
            template: PdfDocumentTemplate[AcceptedRequest],
            generator: PdfDocumentGenerator[ValidatedDocument],
            extender: PdfDocumentExtender[EitherNelErr[ValidatedDocument]]) {

  import Converters._
  import PdfDocumentExtender.ops._
  import PdfDocumentGenerator.ops._
  import PdfDocumentTemplate.ops._
  import ServiceConnector.ops._
  import ServiceResponse.ops._
  import Validator.ops._

  private val decimalFormatter = new DecimalFormat("00")
  private val timeFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")

  val materialize = Flow[ByteString].fold(ByteString.empty) {
    case (acc, b) => acc ++ b
  }.map(_.utf8String)

  val validateApiKey = Flow[IncomingRequest].map {
    case request => request.key.validate().map(key => AcceptedRequest(request.template, request.payload, key))
  }

  val findPdfDocumentTemplate = Flow[EitherNelErr[AcceptedRequest]].map {
    case request => {
      request.
        flatMap(req => req.find().toEitherNel(404, s"Unrecognised template '${req.template}'")).
        flatMap(template => request.map(ar => ValidRequest(template, ar.payload)))
    }
  }

  val validatePayloadWithJsonSchema = Flow[EitherNelErr[ValidRequest]].map {
    case request => {
      for {
        validated <- request
        payload <- request.toOption.map(x => PayloadWithSchema(x.payload, x.template.schema)).validate()
      } yield ValidatedDocument(payload, validated.template)
    }
  }

  val addDateOfIssue = Flow[EitherNelErr[ValidatedDocument]].map {
    _.extend()
  }

  val generatePdfDocument = Flow[EitherNelErr[ValidatedDocument]].map {
    case request => request.map(_.create())
  }

  private[this] def partitionRequests[A](ports: Int) = Partition[EitherNelErr[A]](ports, _ match {
    case Left(_) => 0
    case Right(_) => 1
  })

  private[this] val requestPdfDocumentSigning = Flow[EitherNelErr[UnsignedPdfDocument]].map {
    //it's always right projection after partitioning
    case request => (request.map(_.request()).getOrElse(HttpRequest()), request)
  }

  private[this] val pdfSignatures = connector.connectionPool()

  val signedPdfDocument = Flow[(Try[HttpResponse], EitherNelErr[UnsignedPdfDocument])].mapAsyncUnordered(1) {
    case (httpResponse, request) => {
      httpResponse match {
        case Success(response) => {
          request match {
            case Left(err) => Future.successful(Left(err))
            case Right(_) => response.parse()
          }
        }
        case Failure(exception) => Future.failed(exception)
      }
    }
  }

  def licenseUsage(time: Date): Flow[EitherNelErr[UnsignedPdfDocument], ByteString, NotUsed] = Flow[EitherNelErr[UnsignedPdfDocument]].map {
    case _ => {
      import io.circe.generic.auto._
      import io.circe.syntax._
      val timestamp = timeFormatter.format(time)
      ByteString(LicenseUsage(config.env, timestamp).asJson.spaces2)
    }
  }

  def licenseTrueUp(time: Date): Sink[ByteString, Future[MultipartUploadResult]] = {
    val timestamp = timeFormatter.format(time)
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = decimalFormatter.format(1+calendar.get(Calendar.MONTH))
    val day = decimalFormatter.format(calendar.get(Calendar.DAY_OF_MONTH))
    val key = s"$year/$month/$day/${config.env}-$timestamp.json"
    system.log.debug(s"License event to be stored: $key")
    S3.multipartUpload(config.licenseTrueUpBucket, key).withAttributes(S3Attributes.settings(useStsProvider))
  }

  val stsClient = StsClient.builder().region(Region.EU_WEST_2).build()

  val stsProvider = StsAssumeRoleCredentialsProvider.builder().stsClient(stsClient).refreshRequest(AssumeRoleRequest.builder().roleSessionName(UUID.randomUUID().toString).roleArn("arn:aws:iam::979211549557:role/non-repudiation-pdf-generation-usage").build()).build()

  val useStsProvider = S3Ext(system).settings.withCredentialsProvider(stsProvider)

  def createPdfDocument: Flow[EitherNelErr[ValidatedDocument], EitherNelErr[UnsignedPdfDocument], NotUsed] = Flow.fromGraph(
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val createPdfDocumentShape = builder.add(generatePdfDocument)
      val time = Calendar.getInstance().getTime
      val broadcastCalls = builder.add(Broadcast[EitherNelErr[UnsignedPdfDocument]](2))
      createPdfDocumentShape ~> broadcastCalls
      broadcastCalls.out(0) ~> licenseUsage(time).log("license true-up") ~> licenseTrueUp(time)

      FlowShape(createPdfDocumentShape.in, broadcastCalls.out(1))
    }
  )

  val signPdfDocument = Flow.fromGraph(
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val partitionCalls = builder.add(partitionRequests[UnsignedPdfDocument](2))
      val mergeCalls = builder.add(EitherStage[UnsignedPdfDocument, EitherNelErr[UnsignedPdfDocument], EitherNelErr[SignedPdfDocument]])

      partitionCalls ~> mergeCalls.in0
      partitionCalls ~> requestPdfDocumentSigning ~> pdfSignatures ~> signedPdfDocument ~> mergeCalls.in1

      FlowShape(partitionCalls.in, mergeCalls.out.map(_.flatten).outlet)
    }
  )

}
