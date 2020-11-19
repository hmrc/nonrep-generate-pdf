package uk.gov.hmrc.nonrep.pdfs
package service

import java.util.Base64

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class LicenseManagerSpecs extends AnyWordSpec with Matchers with MockFactory with ScalaFutures {

  import TestServices._

  "licence manager" should {
    "return None when license is not available" in {
      LicenseManager.useLicense(config.appName, None) shouldBe None
    }
  }
}
