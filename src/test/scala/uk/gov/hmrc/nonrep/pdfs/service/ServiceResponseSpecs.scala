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
      val response = HttpResponse(StatusCodes.OK, Seq(RawHeader(TransactionIdHeader, transactionId)), HttpEntity(sampleRequests("trusts-5mld-1-0-0")))
      whenReady(response.parse()(typedSystem, config)) {
        signedPdf => {
          signedPdf.isRight shouldBe true
          signedPdf.toOption.get.transactionId shouldBe transactionId
        }
      }
    }

  }

}
