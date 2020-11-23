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
    val templateId = "trusts-5mld-1-0-0"
    val payload = sampleRequestPayload(templateId)

    "return error for invalid schema validation" in {
      val template = config.templates(apiKeyHash).find(_.id == "trusts-5mld-0-6-0").get
      val payloadWithSchema = Some(PayloadWithSchema(payload, template.schema))
      payloadWithSchema.validate().isLeft shouldBe true
    }

    "be successful on payload validation with correct schema" in {
      val template = config.templates(apiKeyHash).find(_.id == templateId).get
      val payloadWithSchema = Some(PayloadWithSchema(payload, template.schema))
      payloadWithSchema.validate().isRight shouldBe true
    }

  }

}
