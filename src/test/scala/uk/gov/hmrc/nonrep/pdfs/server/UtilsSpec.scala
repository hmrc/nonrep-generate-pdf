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

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.StandardRoute
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.nonrep.pdfs.utils.JsonFormats

class JsonResponseServiceSpec extends AnyWordSpec with Matchers {

  "Json response service" should {
    import JsonFormats._
    import JsonResponseService._
    import JsonResponseService.ops._

    "convert ErrorMessage object into akka-http StandardRoute object" in {
      ErrorMessage("test").completeAsJson(StatusCodes.InternalServerError) shouldBe a[StandardRoute]
    }

    "convert BuildVersion object into akka-http StandardRoute object" in {
      BuildVersion("test").completeAsJson(StatusCodes.OK) shouldBe a[StandardRoute]
    }

  }
}
