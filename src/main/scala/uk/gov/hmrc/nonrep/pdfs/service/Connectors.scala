package uk.gov.hmrc.nonrep.pdfs
package service

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.util.ByteString
import cats.data.NonEmptyList
import uk.gov.hmrc.nonrep.pdfs.model._
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

  implicit val defaultSignaturesServiceConnector: ServiceConnector[UnsignedPdfDocument] = new ServiceConnector[UnsignedPdfDocument]() {
    override def connectionPool()(implicit system: ActorSystem[_], config: ServiceConfig): ServiceCall[UnsignedPdfDocument] =
      if (config.isSignaturesServiceSecure)
        Http().cachedHostConnectionPoolHttps[EitherNelErr[UnsignedPdfDocument]](config.signaturesServiceHost, config.signaturesServicePort)
      else
        Http().cachedHostConnectionPool[EitherNelErr[UnsignedPdfDocument]](config.signaturesServiceHost, config.signaturesServicePort)

    override def request(value: UnsignedPdfDocument)(implicit system: ActorSystem[_], config: ServiceConfig): HttpRequest =
      createRequest(value)

  }

  private[this] def createRequest(value: UnsignedPdfDocument)(implicit system: ActorSystem[_], config: ServiceConfig): HttpRequest = {
    import HeadersConversion._

    val headers = List(RawHeader("Connection", "close"), RawHeader(TransactionIdHeader, value.transactionId))
    HttpRequest(HttpMethods.POST, s"/${config.signaturesServiceHost}/pades/${value.profile}", headers, HttpEntity(value.pdf))
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
      val profileName = response.headers.filter(_.name() == ProfileNameHeader.name).map(_.value()).headOption.getOrElse("")

      if (response.status == StatusCodes.OK) {
        response.entity.dataBytes.
          runFold(ByteString.empty)(_ ++ _).
          map(doc => Right(SignedPdfDocument(doc.toArray[Byte], transactionId, profileName)))
      } else {
        response.discardEntityBytes()
        val error = s"Response status ${response.status} from signatures service ${config.signaturesServiceHost}"
        Future.successful(Left(NonEmptyList.one(ErrorResponse(response.status, error))))
      }
    }
  }
}