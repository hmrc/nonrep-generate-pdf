package uk.gov.hmrc.nonrep.pdfs
package service

import cats.data.NonEmptyList

object Converters {

  implicit class EitherConversions[A <: {def getMessage() : String}, B](e: Either[A, B]) {

    import scala.language.reflectiveCalls

    def toEitherNel(code: Int): EitherNelErr[B] = {
      e.left.map(x => NonEmptyList.one(ErrorResponse(code, x.getMessage())))
    }
  }

  implicit class OptionConversions[T](v: Option[T]) {
    def toEither(error: String): EitherErr[T] = v match {
      case Some(data) => Right(data)
      case _ => Left(ErrorMessage(s"$error"))
    }

    def toEitherNel(code: Int, error: String): EitherNelErr[T] = v match {
      case Some(data) => Right(data)
      case _ => Left(NonEmptyList.one(ErrorResponse(code, s"$error")))
    }
  }

}
