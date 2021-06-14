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
    "have template name accessible for pdf document template v1.0.0" in {
      config.templates(apiKeyHash).filter(_.id == "trusts-5mld-1-0-0").head.name shouldBe "api-584-v1.0.0"
    }
    "have template name accessible for pdf document template v1.1.0" in {
      config.templates(apiKeyHash).filter(_.id == "trusts-5mld-1-1-0").head.name shouldBe "api-584-v1.1.0"
    }
    "have template name accessible for pdf document template v1.2.0" in {
      config.templates(apiKeyHash).filter(_.id == "trusts-5mld-1-2-0").head.name shouldBe "api-584-v1.2.0"
    }
  }
}
