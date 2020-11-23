package uk.gov.hmrc.nonrep.pdfs
package service

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
    "generate PDF document for validated input" in {
      val templateId = "trusts-5mld-1-0-0"
      val payload = sampleRequestPayload(templateId)
      val docTemplate = config.templates(apiKeyHash).find(_.id == templateId).head
      val request = ValidatedDocument(PayloadWithSchema(payload, docTemplate.schema), docTemplate)
      val response = request.create()
      response.pdf.length should be > 0
      response.transactionId shouldBe payload.calculateHash()
    }
  }

}
