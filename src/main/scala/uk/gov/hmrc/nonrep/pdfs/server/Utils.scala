package uk.gov.hmrc.nonrep.pdfs
package server

import java.math.BigInteger
import java.security.MessageDigest

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCode}
import akka.http.scaladsl.server.StandardRoute
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import io.circe.Encoder
import io.circe.syntax._

object Utils {

  implicit class PayloadWithSha256(data: Payload) {
    def calculatePayloadHash = {
      val hash = MessageDigest.getInstance("SHA-256").digest(data.getBytes("UTF-8"))
      String.format("%032x", new BigInteger(1, hash))
    }
  }

}

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

  private def completeResponse[A](value: A, code: StatusCode)(implicit enc: Encoder[A]) = complete(
    HttpResponse(
      code,
      entity = HttpEntity(ContentTypes.`application/json`, value.asJson.noSpaces)
    )
  )

}
