package uk.gov.hmrc.nonrep.pdfs
package service

import java.util.UUID

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.model.HttpMethods
import akka.stream.scaladsl.Flow
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.nonrep.pdfs.model.{TransactionIdHeader, UnsignedPdfDocument}
import uk.gov.hmrc.nonrep.pdfs.server.ServiceConfig

class ServiceConnectorSpecs extends AnyWordSpec with Matchers with MockFactory with ScalaFutures {

  import ServiceConnector.ops._
  import TestServices._

  lazy val testKit = ActorTestKit()

  implicit val typedSystem = testKit.system

  implicit val config: ServiceConfig = new ServiceConfig()
  
  "Service connector" should {
    "create connection request" in {
      val transactionId = UUID.randomUUID().toString
      val template = config.templates(apiKeyHash).find(_.id == "trusts-5mld-0-6-0").get
      val pdf = UnsignedPdfDocument(transactionId, template.profile, sampleRequest_1_0_0)
      val request = pdf.request()(typedSystem, config)
      request.method shouldBe HttpMethods.POST
      request.uri.toString().contains(template.profile) shouldBe true
      request.headers.find(_.name() == TransactionIdHeader.name) shouldBe defined
    }

    "create service connection pool" in {
      val transactionId = UUID.randomUUID().toString
      val template = config.templates(apiKeyHash).find(_.id == "trusts-5mld-0-6-0").get
      val pdf = UnsignedPdfDocument(transactionId, template.profile, sampleRequest_1_0_0)
      val pool = pdf.connectionPool()(typedSystem, config)
      pool.isInstanceOf[Flow[_, _, _]] shouldBe true
      pool.isInstanceOf[ServiceCall[_]] shouldBe true
    }

  }

}
