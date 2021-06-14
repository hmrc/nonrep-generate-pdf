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

class HashCalculatorSpecs extends AnyWordSpec with Matchers with MockFactory with ScalaFutures {

  import HashCalculator.ops._
  import TestServices._

  "Hash calculator for API key" should {
    "generate api key hash" in {
      apiKey.calculateHash() shouldBe "c93ea5ce61e3e92dc024536702a7375a5f4abc85556deae83375afcc6dead40f"
    }
  }

  "Hash calculator for Payload" should {
    "generate payload hash" in {
      val templateId = "trusts-5mld-1-0-0"
      val payload = sampleRequestPayload(templateId)
      val template = config.templates(apiKeyHash).find(_.id == templateId).get
      val payloadWithSchema = PayloadWithSchema(payload, template.schema)
      payloadWithSchema.calculateHash() shouldBe "a3ebe1510e0fbca036d9b14ad8600eaa24c57eb4057dcff4e83f0e95eeee0695"
    }
  }

}
