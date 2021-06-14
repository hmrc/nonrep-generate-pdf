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

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.nonrep.pdfs.model.PayloadWithSchema

class ValidatorSpecs extends AnyWordSpec with Matchers with MockFactory with ScalaFutures {

  import TestServices._
  import Validator.ops._

  "Api key validator" should {
    "return error for empty api key" in {
      val key: Option[ApiKey] = None
      key.validate().isLeft shouldBe true
    }

    "return error for invalid api key" in {
      val key = Some("apiKey")
      key.validate().isLeft shouldBe true
    }

    "be successful on valid api key" in {
      val key = Some(apiKey)
      key.validate().isRight shouldBe true
    }
  }

  "Payload JSON schema validator" should {
    "return error for invalid schema validation" in {
      val templateId = "trusts-5mld-1-0-0"
      val payload = sampleRequestPayload(templateId)
      val template = config.templates(apiKeyHash).find(_.id == "trusts-5mld-0-6-0").get
      val payloadWithSchema = Some(PayloadWithSchema(payload, template.schema))
      payloadWithSchema.validate().isLeft shouldBe true
    }

    "be successful on payload validation with correct schema v1.0.0" in {
      val templateId = "trusts-5mld-1-0-0"
      val payload = sampleRequestPayload(templateId)
      val template = config.templates(apiKeyHash).find(_.id == templateId).get
      val payloadWithSchema = Some(PayloadWithSchema(payload, template.schema))
      payloadWithSchema.validate().isRight shouldBe true
    }

    "be successful on payload validation with correct schema v1.1.0" in {
      val templateId = "trusts-5mld-1-1-0"
      val payload = sampleRequestPayload(templateId)
      val template = config.templates(apiKeyHash).find(_.id == templateId).get
      val payloadWithSchema = Some(PayloadWithSchema(payload, template.schema))
      payloadWithSchema.validate().isRight shouldBe true
    }

    "be successful on payload validation with correct schema v1.2.0" in {
      val templateId = "trusts-5mld-1-2-0"
      val payload = sampleRequestPayload(templateId)
      val template = config.templates(apiKeyHash).find(_.id == templateId).get
      val payloadWithSchema = Some(PayloadWithSchema(payload, template.schema))
      payloadWithSchema.validate().isRight shouldBe true
    }

  }

}
