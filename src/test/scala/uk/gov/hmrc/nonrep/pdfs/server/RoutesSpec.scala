package uk.gov.hmrc.nonrep.pdfs
package server

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.util.ByteString
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration._
import scala.language.postfixOps

class RoutesSpec extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  import TestServices._

  implicit val config = new ServiceConfig()
  lazy val testKit = ActorTestKit()
  implicit def typedSystem = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem = testKit.system.toClassic
  implicit val timeout = RouteTestTimeout(3 second span)

  val routes = Routes().serviceRoutes
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

    "return 404 (not found) for unknown template" in {
      val request = Post(s"/$service/template/unknown/signed-pdf").withEntity(HttpEntity(testPayload))
      request ~> routes ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }

    "accept request for generating pdf with valid template" in {
      //TODO: update when config is complete
      val validTemplate = "interim"
      val request = Post(s"/$service/template/$validTemplate/signed-pdf").withEntity(HttpEntity(testPayload))
      request ~> routes ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/octet-stream`
      }
    }

  }

}