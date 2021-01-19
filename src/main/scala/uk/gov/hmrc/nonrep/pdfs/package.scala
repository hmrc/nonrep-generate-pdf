package uk.gov.hmrc.nonrep

import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCode, StatusCodes}
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import cats.data.EitherNel

import scala.util.Try

package object pdfs {
  type TemplateId = String
  type TemplateName = String
  type JSONSchema = String
  type ApiKey = String
  type Hash = String
  type Payload = String
  type PdfTemplate = Array[Byte]
  type PdfDocument = Array[Byte]
  type PAdESProfile = String
  type TransactionID = String
  type EitherErr[T] = Either[ErrorMessage, T]
  type EitherNelErr[T] = EitherNel[ErrorResponse, T]
  type ServiceCall[A] = Flow[(HttpRequest, EitherNelErr[A]), (Try[HttpResponse], EitherNelErr[A]), Any]

  case class BuildVersion(version: String) extends AnyVal

  case class ErrorMessage(message: String) extends AnyVal

  case class ErrorResponse(error: ErrorMessage, code: StatusCode = StatusCodes.BadRequest, exception: Option[Throwable] = None)

  object ErrorResponse {
    def apply(code: Int, message: String): ErrorResponse = ErrorResponse(ErrorMessage(message), StatusCode.int2StatusCode(code))

    def apply(code: StatusCode, message: String): ErrorResponse = ErrorResponse(ErrorMessage(message), code)
  }

  case class LicenseInfo(licenseType: String, expire: String, key: String, version: String, comment: String)

  case class LicenseUsage(env: String, timestamp: String, count: Int = 1)
}
