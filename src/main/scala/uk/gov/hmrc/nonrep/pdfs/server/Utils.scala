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
package server

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCode}
import akka.http.scaladsl.server.StandardRoute
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import cats.data.NonEmptyList
import io.circe.Encoder
import io.circe.syntax._

trait JsonResponseService[A] {
  def completeAsJson(value: A, code: StatusCode)(implicit enc: Encoder[A]): StandardRoute
}

object JsonResponseService {

  def apply[A](implicit service: JsonResponseService[A]): JsonResponseService[A] = service

  object ops {

    implicit class MessageServiceOps[A: JsonResponseService](message: A) {
      def completeAsJson(code: StatusCode)(implicit enc: Encoder[A]): StandardRoute = JsonResponseService[A].completeAsJson(message, code)
    }

  }

  implicit val defaultErrorMessageService: JsonResponseService[ErrorMessage] = new JsonResponseService[ErrorMessage]() {
    override def completeAsJson(value: ErrorMessage, code: StatusCode)(implicit enc: Encoder[ErrorMessage]): StandardRoute =
      completeResponse(value, code)
  }

  implicit val defaultBuildVersionService: JsonResponseService[BuildVersion] = new JsonResponseService[BuildVersion]() {
    override def completeAsJson(value: BuildVersion, code: StatusCode)(implicit enc: Encoder[BuildVersion]): StandardRoute =
      completeResponse(value, code)
  }

  implicit val defaultNonEmptyListService: JsonResponseService[NonEmptyList[ErrorMessage]] = new JsonResponseService[NonEmptyList[ErrorMessage]]() {
    override def completeAsJson(value: NonEmptyList[ErrorMessage], code: StatusCode)(implicit enc: Encoder[NonEmptyList[ErrorMessage]]): StandardRoute =
      completeResponse(value, code)
  }

  private def completeResponse[A](value: A, code: StatusCode)(implicit enc: Encoder[A]) = complete(
    HttpResponse(
      code,
      entity = HttpEntity(ContentTypes.`application/json`, value.asJson.noSpaces)
    )
  )

}
