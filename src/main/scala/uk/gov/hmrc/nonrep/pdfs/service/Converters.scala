package uk.gov.hmrc.nonrep.pdfs
package service

import java.math.BigInteger
import java.security.MessageDigest

object Converters {

  implicit class PayloadWithSha256(data: Payload) {
    def calculatePayloadHash = {
      val hash = MessageDigest.getInstance("SHA-256").digest(data.getBytes("UTF-8"))
      String.format("%032x", new BigInteger(1, hash))
    }
  }

  implicit class ApiKeySha256(data: ApiKey) {
    def calculateHash = {
      val hash = MessageDigest.getInstance("SHA-256").digest(data.getBytes("UTF-8"))
      String.format("%032x", new BigInteger(1, hash))
    }
  }

  implicit class OptionConversions[T](v: Option[T]) {
    def toEither(error: String): EitherErr[T] = v match {
      case Some(data) => Right(data)
      case _ => Left(ErrorMessage(s"$error"))
    }
    def toEitherResponse(code: Int, error: String): EitherResponse[T] = v match {
      case Some(data) => Right(data)
      case _ => Left(ErrorResponse(code, s"$error"))
    }
  }

}
