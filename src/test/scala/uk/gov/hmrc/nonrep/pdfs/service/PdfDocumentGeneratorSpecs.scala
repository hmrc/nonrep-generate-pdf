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

import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.nonrep.pdfs.model.{PayloadWithSchema, ValidatedDocument}

class PdfDocumentGeneratorSpecs extends AnyWordSpec with Matchers with MockFactory with ScalaFutures {
  import HashCalculator.ops._
  import PdfDocumentGenerator.ops._
  import TestServices._

  "PDF generator service" should {
    "generate PDF document for validated v1.0.0 input" in {
      val templateId = "trusts-5mld-1-0-0"
      val payload = sampleRequestPayload(templateId)
      val docTemplate = config.templates(apiKeyHash).find(_.id == templateId).head
      val request = ValidatedDocument(PayloadWithSchema(payload, docTemplate.schema), docTemplate)
      val response = request.create()
      response.pdf.length should be > 0
      response.transactionId shouldBe payload.calculateHash()
    }

    "generate PDF document for validated v1.1.0 input" in {
      val templateId = "trusts-5mld-1-1-0"
      val payload = sampleRequestPayload(templateId)
      val docTemplate = config.templates(apiKeyHash).find(_.id == templateId).head
      val request = ValidatedDocument(PayloadWithSchema(payload, docTemplate.schema), docTemplate)
      val response = request.create()
      response.pdf.length should be > 0
      response.transactionId shouldBe payload.calculateHash()
    }

    "generate PDF document for validated v1.2.0 input" in {
      val templateId = "trusts-5mld-1-2-0"
      val payload = sampleRequestPayload(templateId)
      val docTemplate = config.templates(apiKeyHash).find(_.id == templateId).head
      val request = ValidatedDocument(PayloadWithSchema(payload, docTemplate.schema), docTemplate)
      val response = request.create()
      response.pdf.length should be > 0
      response.transactionId shouldBe payload.calculateHash()
    }

    "generate PDF document for validated v1.0.0 input with 25 trustees" in {
      val templateId = "trusts-5mld-1-1-0"
      val binary = Files.readAllBytes(new File(getClass.getClassLoader.getResource("1584_sample_1.1.0_25_trustees.json").getFile).toPath())
      val payload = new String(binary, Charset.forName("utf-8"))
      val docTemplate = config.templates(apiKeyHash).find(_.id == templateId).head
      val request = ValidatedDocument(PayloadWithSchema(payload, docTemplate.schema), docTemplate)
      val response = request.create()
      response.pdf.length should be > 0
      response.transactionId shouldBe payload.calculateHash()
    }
  }

}
