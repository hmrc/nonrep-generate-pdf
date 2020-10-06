package uk.gov.hmrc.nonrep.pdfs
package server

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, StandardRoute}
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import uk.gov.hmrc.nonrep.BuildInfo
import uk.gov.hmrc.nonrep.pdfs.model.{GeneratePdfRequest, Template}
import uk.gov.hmrc.nonrep.pdfs.service.Documents

case class Routes()(implicit val system: ActorSystem[_], config: ServiceConfig){

  import JsonResponseService.ops._
  import Documents._
  import Utils._

  val log = system.log

  val serviceName = "generate-pdf"

  private def serviceRoute(template: Template) = {
    //TODO: replace it
    entity(as[Payload]) { payload =>

      val request = GeneratePdfRequest(payload.calculatePayloadHash, template)
      createPdfDocument(request).fold[StandardRoute](
        err => {
          log.error("PDF document creating error - {}", err.message)
          err.completeAsJson(StatusCodes.InternalServerError)
        },
        pdf => {
          log.info("PDF document '{}' generated", pdf.hash)
          complete {
            HttpResponse(
              status = StatusCodes.OK,
              entity = HttpEntity(ContentTypes.`application/octet-stream`, pdf.pdf)
            )
          }
        }
      )
    }
  }

  lazy val serviceRoutes: Route =
    pathPrefix(serviceName) {

      //TODO: support and validate x-api-key
      val apiKey = "interim"

      path("template" / Segment / "signed-pdf") { case templateId =>
        post {
          findPdfDocumentTemplate(apiKey, templateId).fold[StandardRoute]({
            val message = s"Unknown template '$templateId'"
            log.warn(message)
            ErrorMessage(message).completeAsJson(StatusCodes.BadRequest)
          })(template => StandardRoute(serviceRoute(template)))
        }
      } ~ pathPrefix("ping") {
        get {
          complete(HttpResponse(StatusCodes.OK, entity = "pong"))
        }
      } ~ pathPrefix("version") {
        pathEndOrSingleSlash {
          get {
            BuildVersion(version = BuildInfo.version).completeAsJson(StatusCodes.OK)
          }
        }
      }
    } ~ pathPrefix("ping") {
      get {
        complete(HttpResponse(StatusCodes.OK, entity = "pong"))
      }
    }

}
