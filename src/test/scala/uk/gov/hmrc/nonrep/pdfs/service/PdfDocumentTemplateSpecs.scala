package uk.gov.hmrc.nonrep.pdfs
package service

import java.nio.charset.Charset

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.nonrep.pdfs.model.AcceptedRequest

class PdfDocumentTemplateSpecs extends AnyWordSpec with Matchers with MockFactory with ScalaFutures {
  import TestServices._
  import PdfDocumentTemplate.ops._

  "PDF documents templates service" should {
    val templateId = "trusts-5mld-1-0-0"
    val payload = sampleRequestPayload(templateId)
    "find existing template" in {
      val request = AcceptedRequest("trusts-5mld-1-0-0", payload, apiKeyHash)
      request.find() shouldBe defined
    }
    "return None for non-existing template" in {
      val request = AcceptedRequest("trusts-5mld-0-0-0", payload, apiKeyHash)
      request.find() shouldBe None
    }
  }
}
