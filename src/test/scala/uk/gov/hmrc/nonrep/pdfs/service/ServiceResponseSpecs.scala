package uk.gov.hmrc.nonrep.pdfs.service

import java.util.UUID

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.nonrep.pdfs.TestServices
import uk.gov.hmrc.nonrep.pdfs.model.{HeadersConversion, TransactionIdHeader}
import uk.gov.hmrc.nonrep.pdfs.server.ServiceConfig

class ServiceResponseSpecs extends AnyWordSpec with Matchers with MockFactory with ScalaFutures {

  import HeadersConversion._
  import ServiceResponse.ops._
  import TestServices._

  lazy val testKit = ActorTestKit()

  implicit val typedSystem = testKit.system

  implicit val config: ServiceConfig = new ServiceConfig()

  "Service response" should {
    "parse response for signed pdf document" in {
      val transactionId = UUID.randomUUID().toString
      val response = HttpResponse(StatusCodes.OK, Seq(RawHeader(TransactionIdHeader, transactionId)), HttpEntity(sampleRequest_1_0_0))
      whenReady(response.parse()(typedSystem, config)) {
        signedPdf => {
          signedPdf.isRight shouldBe true
          signedPdf.toOption.get.transactionId shouldBe transactionId
        }
      }
    }

  }

}
