package uk.gov.hmrc.nonrep.pdfs
package server

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration._
import scala.language.postfixOps
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.model.{HttpEntity, StatusCodes}
import akka.util.ByteString

class RoutesSpec extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  implicit val config = new ServiceConfig()
  lazy val testKit = ActorTestKit()
  implicit def typedSystem = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem = testKit.system.toClassic
  implicit val timeout = RouteTestTimeout(3 second span)

  val routes = Routes()
  val serviceRoutes = routes.serviceRoutes

  "Service routes" should {
    
    "return pong response on /ping endpoint" in {
      val request = Get("/ping")
      request ~> serviceRoutes ~> check {
        status shouldBe StatusCodes.OK
        whenReady((responseEntity.dataBytes.runFold(ByteString.empty)(_ ++ _).map(_.utf8String))) {
          response => response shouldBe "pong"
        }
      }
    }

    "return build information on /version endpoint" in {
      val request = Get(s"/${routes.serviceName}/version")
      request ~> serviceRoutes ~> check {
        status shouldBe StatusCodes.OK
        whenReady((responseEntity.dataBytes.runFold(ByteString.empty)(_ ++ _).map(_.utf8String))) {
          response => response.contains("\"version\"") shouldBe true
        }
      }
    }

  }

}

/*
      val request = Post(s"test").withEntity(HttpEntity("test"))

      request ~> BuildVersion("0.1.0").completeAsJson(StatusCodes.OK) ~> check {
        status shouldBe StatusCodes.OK
        whenReady((responseEntity.dataBytes.runFold(ByteString(""))(_ ++ _).map(_.utf8String))) {
          x => println(x)
        }
      }


 */