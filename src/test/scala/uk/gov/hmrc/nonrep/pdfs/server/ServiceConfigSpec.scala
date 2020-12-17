package uk.gov.hmrc.nonrep.pdfs
package server

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.nonrep.pdfs.TestServices._

class ServiceConfigSpec extends AnyWordSpec with Matchers {

  "ServiceConfig" should {
    "specify app name" in {
      config.appName shouldBe "generate-pdf"
    }
    "specify environment" in {
      config.env should not be empty
    }
    "be able to use default service port" in {
      config.servicePort shouldBe config.defaultPort
    }
    "load DITO license if available" in {
      config.licenseInfo shouldBe None
    }
    "load signatures service uri" in {
      config.signaturesServiceUri.isAbsolute shouldBe true
      config.signaturesServiceHost should not be empty
      config.signaturesServicePort should be > 0
      config.isSignaturesServiceSecure shouldBe false
    }
    "load pdf document templates" in {
      config.templates.get(apiKeyHash) shouldBe defined
    }
    "have template name accessible for pdf document template" in {
      config.templates(apiKeyHash).filter(_.id == "trusts-5mld-1-0-0").head.name shouldBe "api-584-v1.0.0"
    }
  }
}
