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

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ContentType, ContentTypes, HttpEntity, MediaTypes, StatusCodes}
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
        withEntity(HttpEntity(sampleRequests("trusts-5mld-0-7-0")))
      request ~> routes ~> check {
        status shouldBe StatusCodes.Unauthorized
      }
    }

    "reject request with invalid/unknown x-api-key" in {
      val request = Post(s"/$service/template/unknown/signed-pdf").
        withEntity(HttpEntity(sampleRequests("trusts-5mld-0-7-0"))).withHeaders(RawHeader("X-API-Key", "xxx"))
      request ~> routes ~> check {
        status shouldBe StatusCodes.Unauthorized
      }
    }

    "return 404 (not found) for unknown template" in {
      val request = Post(s"/$service/template/whatever/signed-pdf").
        withEntity(HttpEntity(sampleRequests("trusts-5mld-0-6-0"))).
        withHeaders(RawHeader("X-API-Key", apiKey))
      request ~> routes ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }

    "accept request for generating pdf with valid v1.0.0 template" in {
      val template = "trusts-5mld-1-0-0"
      val request = Post(s"/$service/template/$template/signed-pdf").
        withEntity(HttpEntity(sampleRequests("trusts-5mld-1-0-0"))).
        withHeaders(RawHeader(ApiKeyHeader, apiKey))
      request ~> routes ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentType(MediaTypes.`application/pdf`)
      }
    }

    "accept request for generating pdf with valid v1.1.0 template" in {
      val template = "trusts-5mld-1-1-0"
      val request = Post(s"/$service/template/$template/signed-pdf").
        withEntity(HttpEntity(sampleRequests("trusts-5mld-1-1-0"))).
        withHeaders(RawHeader(ApiKeyHeader, apiKey))
      request ~> routes ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentType(MediaTypes.`application/pdf`)
      }
    }

    "accept request for generating pdf with valid v1.2.0 template" in {
      val template = "trusts-5mld-1-2-0"
      val request = Post(s"/$service/template/$template/signed-pdf").
        withEntity(HttpEntity(sampleRequests("trusts-5mld-1-2-0"))).
        withHeaders(RawHeader(ApiKeyHeader, apiKey))
      request ~> routes ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentType(MediaTypes.`application/pdf`)
      }
    }

    "fail on JSON schema validation with invalid payload" in {
      val template = "trusts-5mld-1-0-0"
      val request = Post(s"/$service/template/$template/signed-pdf").
        withEntity(HttpEntity(sampleRequests("trusts-5mld-0-6-0"))).
        withHeaders(RawHeader(ApiKeyHeader, apiKey))
      request ~> routes ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    "return jvm metrics" in {
      val req = Get("/metrics")
      req ~> routes ~> check {
        status shouldBe StatusCodes.OK
        whenReady(entityToString(response.entity)) { body =>
          body
            .split('\n')
            .filter(_.startsWith("# TYPE ")) should contain allElementsOf Seq(
            "# TYPE jvm_classes_loaded gauge",
            "# TYPE jvm_classes_loaded_total counter",
            "# TYPE jvm_classes_unloaded_total counter",
            "# TYPE jvm_memory_pool_allocated_bytes_total counter",
            "# TYPE jvm_buffer_pool_used_bytes gauge",
            "# TYPE jvm_buffer_pool_capacity_bytes gauge",
            "# TYPE jvm_buffer_pool_used_buffers gauge",
            "# TYPE jvm_memory_bytes_used gauge",
            "# TYPE jvm_memory_bytes_committed gauge",
            "# TYPE jvm_memory_bytes_max gauge",
            "# TYPE jvm_memory_bytes_init gauge",
            "# TYPE jvm_memory_pool_bytes_used gauge",
            "# TYPE jvm_memory_pool_bytes_committed gauge",
            "# TYPE jvm_memory_pool_bytes_max gauge",
            "# TYPE jvm_memory_pool_bytes_init gauge",
            "# TYPE jvm_info gauge",
            "# TYPE jvm_threads_current gauge",
            "# TYPE jvm_threads_daemon gauge",
            "# TYPE jvm_threads_peak gauge",
            "# TYPE jvm_threads_started_total counter",
            "# TYPE jvm_threads_deadlocked gauge",
            "# TYPE jvm_threads_deadlocked_monitor gauge",
            "# TYPE jvm_threads_state gauge",
            "# TYPE jvm_gc_collection_seconds summary"
          )
        }
      }
    }

  }

}