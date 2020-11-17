package uk.gov.hmrc.nonrep.pdfs
package service

import cats.data.NonEmptyList
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.nonrep.pdfs.{EitherNelErr, ErrorMessage, TestServices}
import uk.gov.hmrc.nonrep.pdfs.model.{PayloadWithSchema, ValidatedDocument}

class PdfDocumentExtenderSpecs extends AnyWordSpec with Matchers with MockFactory with ScalaFutures {
  import TestServices._
  import PdfDocumentExtender._
  import PdfDocumentExtender.ops._

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
