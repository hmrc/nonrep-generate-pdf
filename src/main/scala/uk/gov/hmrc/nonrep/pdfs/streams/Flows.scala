package uk.gov.hmrc.nonrep.pdfs
package streams

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.FlowShape
import akka.stream.scaladsl.{Flow, GraphDSL, Partition}
import akka.util.ByteString
import uk.gov.hmrc.nonrep.pdfs.model._
import uk.gov.hmrc.nonrep.pdfs.server.ServiceConfig
import uk.gov.hmrc.nonrep.pdfs.service.{Converters, PdfDocumentGenerator, PdfDocumentTemplate, ServiceConnector, ServiceResponse, Validator}

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
              generator: PdfDocumentGenerator[ValidatedDocument]) = new Flows()
}

class Flows(implicit val system: ActorSystem[_],
            config: ServiceConfig,
            apiKeyValidator: Validator[ApiKey],
            payloadValidator: Validator[PayloadWithSchema],
            connector: ServiceConnector[UnsignedPdfDocument],
            parser: ServiceResponse[SignedPdfDocument],
            template: PdfDocumentTemplate[AcceptedRequest],
            generator: PdfDocumentGenerator[ValidatedDocument]) {

  import Converters._
  import ServiceConnector.ops._
  import ServiceResponse.ops._
  import Validator.ops._
  import PdfDocumentTemplate.ops._
  import PdfDocumentGenerator.ops._

  val materialize = Flow[ByteString].fold(ByteString.empty) {
    case (acc, b) => acc ++ b
  }.map(_.utf8String)

  val validateApiKey = Flow[IncomingRequest].map {
    case request => request.key.validate().map(key => AcceptedRequest(request.template, request.payload, key))
  }

  val findPdfDocumentTemplate = Flow[EitherNelErr[AcceptedRequest]].map {
    case request => {
      request.
        flatMap(req => req.find().toEitherNel(404, s"Unknown template '$req.template'")).
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

  val createPdfDocument = Flow[EitherNelErr[ValidatedDocument]].map {
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
