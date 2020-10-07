package uk.gov.hmrc.nonrep.pdfs
package server

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.StandardRoute
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.nonrep.pdfs.utils.JsonFormats

class JsonResponseServiceSpec extends AnyWordSpec with Matchers {

  "Json response service" should {
    import JsonResponseService._
    import JsonResponseService.ops._
    import JsonFormats._

    "convert ErrorMessage object into akka-http StandardRoute object" in {
      ErrorMessage("test").completeAsJson(StatusCodes.InternalServerError) shouldBe a [StandardRoute]
    }

    "convert BuildVersion object into akka-http StandardRoute object" in {
      BuildVersion("test").completeAsJson(StatusCodes.OK) shouldBe a [StandardRoute]
    }

  }
}
