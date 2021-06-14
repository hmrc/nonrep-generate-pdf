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
      val templateId = "trusts-5mld-0-6-0"
      val template = config.templates(apiKeyHash).find(_.id == templateId).get
      val pdf = UnsignedPdfDocument(transactionId, template.profile, sampleRequests("trusts-5mld-1-0-0"), 1)
      val request = pdf.request()(typedSystem, config)
      request.method shouldBe HttpMethods.POST
      request.uri.toString().contains(template.profile) shouldBe true
      request.headers.find(_.name() == TransactionIdHeader.name) shouldBe defined
    }

    "create service connection pool" in {
      val transactionId = UUID.randomUUID().toString
      val templateId = "trusts-5mld-0-6-0"
      val template = config.templates(apiKeyHash).find(_.id == templateId).get
      val pdf = UnsignedPdfDocument(transactionId, template.profile, sampleRequests("trusts-5mld-1-0-0"), 1)
      val pool = pdf.connectionPool()(typedSystem, config)
      pool.isInstanceOf[Flow[_, _, _]] shouldBe true
      pool.isInstanceOf[ServiceCall[_]] shouldBe true
    }

  }

}
