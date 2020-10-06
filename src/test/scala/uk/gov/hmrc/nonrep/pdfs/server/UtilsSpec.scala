package uk.gov.hmrc.nonrep.pdfs
package server

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.StandardRoute
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class JsonResponseServiceSpec extends AnyWordSpec with Matchers {

  "Error response service" should {
    import JsonResponseService._
    import JsonResponseService.ops._
    import Utils._

    "convert ErrorMessage object into akka-http StandardRoute object" in {
      ErrorMessage("test").completeAsJson(StatusCodes.InternalServerError) shouldBe a [StandardRoute]
    }

    "convert BuildVersion object into akka-http StandardRoute object" in {
      BuildVersion("test").completeAsJson(StatusCodes.OK) shouldBe a [StandardRoute]
    }

  }
}
