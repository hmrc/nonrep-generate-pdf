/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

  implicit class LeftConversions[A, B](v: Either[A, B]) {
    def withRight[B1]: Either[A, B1] = (v: @unchecked) match {
      case Left(x) => Left(x)
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
