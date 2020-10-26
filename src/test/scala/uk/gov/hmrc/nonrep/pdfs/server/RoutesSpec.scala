package uk.gov.hmrc.nonrep.pdfs
package server

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.util.ByteString
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.nonrep.pdfs.model.{ApiKeyHeader, HeadersConversion}

import scala.concurrent.duration._
import scala.language.postfixOps

class RoutesSpec extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  import HeadersConversion._
  import TestServices._

  lazy val testKit = ActorTestKit()

  implicit def typedSystem = testKit.system

  override def createActorSystem(): akka.actor.ActorSystem = testKit.system.toClassic

  implicit val timeout = RouteTestTimeout(3 second span)

  val routes = Routes(testFlows).serviceRoutes
  val service = config.appName

  "Service routes" should {

    "return pong response on /ping endpoint" in {
      val request = Get("/ping")
      request ~> routes ~> check {
        status shouldBe StatusCodes.OK
        whenReady((responseEntity.dataBytes.runFold(ByteString.empty)(_ ++ _).map(_.utf8String))) {
          response => response shouldBe "pong"
        }
      }
    }

    "return pong response on service /ping endpoint" in {
      val request = Get(s"/$service/ping")
      request ~> routes ~> check {
        status shouldBe StatusCodes.OK
        whenReady((responseEntity.dataBytes.runFold(ByteString.empty)(_ ++ _).map(_.utf8String))) {
          response => response shouldBe "pong"
        }
      }
    }

    "return build information on /version endpoint" in {
      val request = Get(s"/$service/version")
      request ~> routes ~> check {
        status shouldBe StatusCodes.OK
        whenReady((responseEntity.dataBytes.runFold(ByteString.empty)(_ ++ _).map(_.utf8String))) {
          response => response.contains("\"version\"") shouldBe true
        }
      }
    }

    "reject request without x-api-key" in {
      val request = Post(s"/$service/template/unknown/signed-pdf").
        withEntity(HttpEntity(sampleRequest_0_7_0))
      request ~> routes ~> check {
        status shouldBe StatusCodes.Unauthorized
      }
    }

    "reject request with invalid/unknown x-api-key" in {
      val request = Post(s"/$service/template/unknown/signed-pdf").
        withEntity(HttpEntity(sampleRequest_0_7_0)).withHeaders(RawHeader("X-API-Key", "xxx"))
      request ~> routes ~> check {
        status shouldBe StatusCodes.Unauthorized
      }
    }

    "return 404 (not found) for unknown template" in {
      val request = Post(s"/$service/template/whatever/signed-pdf").
        withEntity(HttpEntity(sampleRequest_0_6_0)).
        withHeaders(RawHeader("X-API-Key", apiKey))
      request ~> routes ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }

    "accept request for generating pdf with valid template" in {
      val template = "trusts-5mld-1-0-0"
      val request = Post(s"/$service/template/$template/signed-pdf").
        withEntity(HttpEntity(sampleRequest_1_0_0)).
        withHeaders(RawHeader(ApiKeyHeader, apiKey))
      request ~> routes ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/octet-stream`
      }
    }

    "fail on JSON schema validation with invalid payload" in {
      val template = "trusts-5mld-1-0-0"
      val request = Post(s"/$service/template/$template/signed-pdf").
        withEntity(HttpEntity(sampleRequest_0_6_0)).
        withHeaders(RawHeader(ApiKeyHeader, apiKey))
      request ~> routes ~> check {
        status shouldBe StatusCodes.BadRequest
      }

    }

  }

}