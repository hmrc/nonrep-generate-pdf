package uk.gov.hmrc.nonrep

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import cats.data.EitherNel

package object pdfs {
  type TemplateId = String
  type JSONSchema = String
  type ApiKey = String
  type Hash = String
  type PdfTemplate = Array[Byte]
  type PdfDocument = Array[Byte]
  type PAdESProfile = String
  type TransactionID = String
  type EitherErr[T] = Either[ErrorMessage, T]
  type EitherNelErr[T] = EitherNel[ErrorResponse, T]

  case class BuildVersion(version: String) extends AnyVal

  case class ErrorMessage(message: String) extends AnyVal

  case class ErrorResponse(error: ErrorMessage, code: StatusCode = StatusCodes.BadRequest, exception: Option[Throwable] = None)

  object ErrorResponse {
    def apply(code: Int, message: String): ErrorResponse = ErrorResponse(ErrorMessage(message), StatusCode.int2StatusCode(code))

    def apply(code: StatusCode, message: String): ErrorResponse = ErrorResponse(ErrorMessage(message), code)
  }

}
