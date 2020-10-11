package uk.gov.hmrc.nonrep.pdfs
package server

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.{Route, StandardRoute}
import uk.gov.hmrc.nonrep.BuildInfo
import uk.gov.hmrc.nonrep.pdfs.model.{ApiKeyHeader, GeneratePdfRequest, HeadersConversion, Template}
import uk.gov.hmrc.nonrep.pdfs.service.{Converters, Documents, Validators}
import uk.gov.hmrc.nonrep.pdfs.utils.JsonFormats

case class Routes()(implicit val system: ActorSystem[_], config: ServiceConfig) {

  import Converters._
  import Documents._
  import HeadersConversion._
  import JsonFormats._
  import JsonResponseService.ops._
  import Validators._

  val log = system.log

  private def serviceRoute(template: Template) = {
    //TODO: replace it
    entity(as[Payload]) { payload =>

      val request = GeneratePdfRequest(payload.calculatePayloadHash, template)
      createPdfDocument(request).fold[StandardRoute](
        err => {
          log.error("PDF document creating error - {}", err.message)
          err.completeAsJson(StatusCodes.InternalServerError)
        },
        response => {
          log.info("PDF document '{}' generated", response.hash)
          complete {
            HttpResponse(
              status = StatusCodes.OK,
              entity = HttpEntity(ContentTypes.`application/octet-stream`, response.pdf)
            )
          }
        }
      )
    }
  }

  lazy val serviceRoutes: Route =
    pathPrefix(config.appName) {

      path("template" / Segment / "signed-pdf") { case templateId =>
        post {
          optionalHeaderValueByName(ApiKeyHeader) { apiKey =>
            validateApiKey(apiKey).flatMap(findPdfDocumentTemplate(_, templateId)).fold[StandardRoute](
              err => {
                log.warn(err.error.message)
                err.error.completeAsJson(err.code)
              },
              response => {
                StandardRoute(serviceRoute(response))
              })
          }
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
  //TODO: metrics

}
