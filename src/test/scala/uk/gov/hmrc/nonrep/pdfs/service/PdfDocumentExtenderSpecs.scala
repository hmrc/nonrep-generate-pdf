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

import cats.data.NonEmptyList
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.nonrep.pdfs.model.{PayloadWithSchema, ValidatedDocument}

class PdfDocumentExtenderSpecs extends AnyWordSpec with Matchers with MockFactory with ScalaFutures {
  import PdfDocumentExtender._
  import PdfDocumentExtender.ops._
  import TestServices._

  "Document extender" should {
    "add date of issue" in {
      val templateId = "trusts-5mld-1-0-0"
      val payload = sampleRequestPayload(templateId)
      val docTemplate = config.templates(apiKeyHash).find(_.id == templateId).head
      val doc = ValidatedDocument(PayloadWithSchema(payload, docTemplate.schema), docTemplate)
      val request = Right(doc).withLeft[NonEmptyList[ErrorResponse]]
      payload.contains("creationDate") shouldBe false
      request.extend().getOrElse(doc).payloadWithSchema.payload.contains("creationDate") shouldBe true
    }
  }

}
