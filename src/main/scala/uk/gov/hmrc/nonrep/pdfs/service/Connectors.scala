package uk.gov.hmrc.nonrep.pdfs
package service

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model._
import akka.util.ByteString
import cats.data.NonEmptyList
import uk.gov.hmrc.nonrep.pdfs.model.{HeadersConversion, SignPdfDocument, SignedPdfDocument, TransactionIdHeader}
import uk.gov.hmrc.nonrep.pdfs.server.ServiceConfig

import scala.concurrent.Future

trait ServiceConnector[A] {
  def request(value: A)(implicit system: ActorSystem[_], config: ServiceConfig): HttpRequest

  def connectionPool()(implicit system: ActorSystem[_], config: ServiceConfig): ServiceCall[A]
}

object ServiceConnector {

  def apply[A](implicit service: ServiceConnector[A]): ServiceConnector[A] = service

  object ops {

    implicit class ServiceConnectorOps[A: ServiceConnector](value: A) {
      def request()(implicit system: ActorSystem[_], config: ServiceConfig) = ServiceConnector[A].request(value)

      def connectionPool()(implicit system: ActorSystem[_], config: ServiceConfig) = ServiceConnector[A].connectionPool()
    }

  }

  implicit val defaultSignaturesServiceConnector: ServiceConnector[SignPdfDocument] = new ServiceConnector[SignPdfDocument]() {
    override def connectionPool()(implicit system: ActorSystem[_], config: ServiceConfig): ServiceCall[SignPdfDocument] =
      if (config.isServiceProtocolSecure)
        Http().cachedHostConnectionPoolHttps[EitherNelErr[SignPdfDocument]](config.signaturesService)
      else
        Http().cachedHostConnectionPool[EitherNelErr[SignPdfDocument]](config.signaturesService)

    override def request(value: SignPdfDocument)(implicit system: ActorSystem[_], config: ServiceConfig): HttpRequest =
      createRequest(value)

  }

  private[this] def createRequest(value: SignPdfDocument)(implicit system: ActorSystem[_], config: ServiceConfig): HttpRequest = {
    import HeadersConversion._

    val headers = List(RawHeader(TransactionIdHeader, value.transactionId))
    HttpRequest(HttpMethods.POST, s"/${config.signaturesService}/pades/${value.profile}", headers, HttpEntity(value.pdf))
  }

}

trait ServiceResponse[A] {
  def parse(response: HttpResponse)(implicit system: ActorSystem[_], config: ServiceConfig): Future[EitherNelErr[A]]
}

object ServiceResponse {
  def apply[A](implicit service: ServiceResponse[A]): ServiceResponse[A] = service

  object ops {

    implicit class ServiceResponseOps[A: ServiceResponse](response: HttpResponse) {
      def parse()(implicit system: ActorSystem[_], config: ServiceConfig) = ServiceResponse[A].parse(response)
    }

  }

  implicit val defaultSignPdfDocumentResponse: ServiceResponse[SignedPdfDocument] = new ServiceResponse[SignedPdfDocument]() {
    override def parse(response: HttpResponse)(implicit system: ActorSystem[_], config: ServiceConfig): Future[EitherNelErr[SignedPdfDocument]] = {
      import system.executionContext

      val transactionId = response.headers.filter(_.name() == TransactionIdHeader.name).map(_.value()).headOption.getOrElse("")
      //val transactionId = response.headers.filter(h => h.name() == TransactionIdHeader.name && !h.value().isEmpty).map(_.value()).headOption

      if (response.status == StatusCodes.OK) {
        response.entity.dataBytes.
          runFold(ByteString.empty)(_ ++ _).
          map(doc => Right(SignedPdfDocument(doc.toArray[Byte], transactionId)))
      } else {
        response.discardEntityBytes()
        val error = s"Response status ${response.status} from signatures service ${config.signaturesService}"
        Future.successful(Left(NonEmptyList.one(ErrorResponse(response.status, error))))
      }
    }
  }
}