package uk.gov.hmrc.nonrep.pdfs
package service

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.nonrep.pdfs.model.PayloadSchema

class ValidatorSpecs extends AnyWordSpec with Matchers with MockFactory with ScalaFutures {

  import Validator.ops._
  import TestServices._

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
      val template = config.templates(apiKeyHash).find(_.id == "trusts-5mld-0-6-0").get
      val payload = Some(PayloadSchema(new String(sampleRequest_1_0_0), template.schema))
      payload.validate().isLeft shouldBe true
    }

    "be successful on payload validation with correct schema" in {
      val template = config.templates(apiKeyHash).find(_.id == "trusts-5mld-1-0-0").get
      val payload = Some(PayloadSchema(new String(sampleRequest_1_0_0), template.schema))
      payload.validate().isRight shouldBe true
    }

  }

}
