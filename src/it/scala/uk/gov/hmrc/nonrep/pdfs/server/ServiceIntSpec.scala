package uk.gov.hmrc.nonrep.pdfs
package server

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import org.scalatest.Inside
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Span}
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.Future

class ServiceIntSpec extends AnyWordSpec with Matchers with ScalatestRouteTest with ScalaFutures with Inside {

  import TestServices._

  var server: NonrepMicroservice = null
  implicit val config: ServiceConfig = new ServiceConfig(9000)
  val hostUrl = s"http://localhost:${config.servicePort}"
  val service = config.appName

  lazy val testKit = ActorTestKit()
  implicit val typedSystem = testKit.system

  override def createActorSystem(): akka.actor.ActorSystem = testKit.system.toClassic

  implicit val patience: PatienceConfig = PatienceConfig(Span(5000, Millis), Span(100, Millis))

  override def beforeAll() = {
    server = NonrepMicroservice(Routes())
  }

  override def afterAll(): Unit = {
    whenReady(server.serverBinding) {
      _.unbind()
    }
  }

  private def entityToString(entity: ResponseEntity) =
    entity.dataBytes.runFold(ByteString.empty)(_ ++ _).map(_.utf8String)

  "generate pdf service" should {
    "return a 'pong' response for GET requests to /ping endpoint" in {
      val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = s"$hostUrl/ping"))
      whenReady(responseFuture) { res =>
        res.status shouldBe StatusCodes.OK
        whenReady(entityToString(res.entity)) { body =>
          body shouldBe "pong"
        }
      }
    }

    "return a 'pong' response for GET requests to service /ping endpoint" in {
      val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = s"$hostUrl/$service/ping"))
      whenReady(responseFuture) { res =>
        res.status shouldBe StatusCodes.OK
        whenReady(entityToString(res.entity)) { body =>
          body shouldBe "pong"
        }
      }
    }

    "return build information for GET requests to service /version endpoint" in {
      val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = s"$hostUrl/$service/version"))
      whenReady(responseFuture) { res =>
        res.status shouldBe StatusCodes.OK
        whenReady(entityToString(res.entity)) { body =>
          body.contains("\"version\"") shouldBe true
        }
      }
    }

    "return 404 (not found) for unknown template" in {
      val request = Post(s"$hostUrl/$service/template/unknown/signed-pdf").
        withEntity(HttpEntity(sampleRequest_0_6_0)).
        withHeaders(RawHeader("X-API-Key", apiKey))
      val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
      whenReady(responseFuture) { res =>
        res.status shouldBe StatusCodes.NotFound
      }
    }

    "accept and respond when valid template requested" in {
      val template = "trusts-5mld-1-0-0"
      val request = Post(s"$hostUrl/$service/template/$template/signed-pdf").
        withEntity(HttpEntity(sampleRequest_1_0_0)).
        withHeaders(RawHeader("X-API-Key", apiKey))
      val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
      whenReady(responseFuture) { res =>
        res.status shouldBe StatusCodes.OK
        res.entity.getContentType() shouldBe ContentTypes.`application/octet-stream`
      }
    }

    "reject request without x-api-key" in {
      val template = "trusts-5mld-1-0-0"
      val request = Post(s"$hostUrl/$service/template/$template/signed-pdf").
        withEntity(HttpEntity(sampleRequest_1_0_0))
      val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
      whenReady(responseFuture) { res =>
        res.status shouldBe StatusCodes.Unauthorized
      }
    }

    "reject request with invalid/unknown x-api-key" in {
      val template = "trusts-5mld-1-0-0"
      val request = Post(s"$hostUrl/$service/template/$template/signed-pdf").
        withEntity(HttpEntity(sampleRequest_1_0_0)).
        withHeaders(RawHeader("X-API-Key", "unknown"))
      val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
      whenReady(responseFuture) { res =>
        res.status shouldBe StatusCodes.Unauthorized
      }
    }

    "fail on JSON schema validation with invalid payload" in {
      val template = "trusts-5mld-1-0-0"
      val request = Post(s"$hostUrl/$service/template/$template/signed-pdf").
        withEntity(HttpEntity(sampleRequest_0_6_0)).
        withHeaders(RawHeader("X-API-Key", apiKey))
      val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
      whenReady(responseFuture) { res =>
        res.status shouldBe StatusCodes.BadRequest
      }
    }

  }
}
