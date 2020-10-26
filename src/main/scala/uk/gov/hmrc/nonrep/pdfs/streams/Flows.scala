package uk.gov.hmrc.nonrep.pdfs
package streams

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.FlowShape
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Partition}
import akka.util.ByteString
import uk.gov.hmrc.nonrep.pdfs.model._
import uk.gov.hmrc.nonrep.pdfs.server.ServiceConfig
import uk.gov.hmrc.nonrep.pdfs.service.{Documents, ServiceConnector, ServiceResponse, Validator}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object Flows {
  def apply()(implicit system: ActorSystem[_],
              config: ServiceConfig,
              apiKeyValidator: Validator[ApiKey],
              payloadKeyValidator: Validator[Payload],
              connector: ServiceConnector[SignPdfDocument],
              parser: ServiceResponse[SignedPdfDocument]) = new Flows()
}

class Flows(implicit val system: ActorSystem[_],
            config: ServiceConfig,
            apiKeyValidator: Validator[ApiKey],
            payloadKeyValidator: Validator[Payload],
            connector: ServiceConnector[SignPdfDocument],
            parser: ServiceResponse[SignedPdfDocument]) {

  import Validator.ops._
  import ServiceConnector.ops._
  import ServiceResponse.ops._

  val materialize = Flow[ByteString].fold(ByteString.empty) {
    case (acc, b) => acc ++ b
  }.map(_.utf8String)

  val validateApiKey = Flow[IncomingRequest].map {
    case request => request.key.validate().map(key => AcceptedRequest(request.template, request.payload, key))
  }

  val findPdfDocumentTemplate = Flow[EitherNelErr[AcceptedRequest]].map {
    case request => Documents.findPdfDocumentTemplate(request).flatMap(template => request.map(ar => ValidatedRequest(template, ar.payload)))
  }

  val validatePayloadWithJsonSchema = Flow[EitherNelErr[ValidatedRequest]].map {
    _.flatMap(request => Some(Payload(request.payload, request.template.schema)).validate().map(GeneratePdfDocument(_, request.template)))
  }

  val createPdfDocument = Flow[EitherNelErr[GeneratePdfDocument]].map {
    case request => Documents.createPdfDocument(request)
  }

  private[this] def partitionRequests[A](ports: Int) = Partition[EitherNelErr[A]](ports, _ match {
    case Left(_) => 0
    case Right(_) => 1
  })

  private[this] val requestPdfDocumentSigning = Flow[EitherNelErr[SignPdfDocument]].map {
    //it's always right after partitioning
    case request => (request.map(_.request()).getOrElse(HttpRequest()), request)
  }

  private[this]val pdfSignatures = connector.connectionPool()

  val signedPdfDocument = Flow[(Try[HttpResponse], EitherNelErr[SignPdfDocument])].mapAsyncUnordered(1) {
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

      val partitionCalls = builder.add(partitionRequests[SignPdfDocument](2))
      val mergeCalls = builder.add(EitherStage[SignPdfDocument, EitherNelErr[SignPdfDocument], EitherNelErr[SignedPdfDocument]])

      partitionCalls ~> mergeCalls.in0
      partitionCalls ~> requestPdfDocumentSigning ~> pdfSignatures ~> signedPdfDocument ~> mergeCalls.in1

      FlowShape(partitionCalls.in, mergeCalls.out.map(_.flatten).outlet)
    }
  )

}
