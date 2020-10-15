package uk.gov.hmrc.nonrep.pdfs
package server

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.{ExceptionHandler, Route, StandardRoute}
import uk.gov.hmrc.nonrep.BuildInfo
import uk.gov.hmrc.nonrep.pdfs.model.{ApiKeyHeader, ClientTemplate, GeneratePdfRequest, HeadersConversion, Payload}
import uk.gov.hmrc.nonrep.pdfs.service._
import uk.gov.hmrc.nonrep.pdfs.utils.JsonFormats

case class Routes()(implicit val system: ActorSystem[_], config: ServiceConfig) {

  import Documents._
  import HeadersConversion._
  import JsonFormats._
  import JsonResponseService.ops._
  import Validator._
  import Validator.ops._

  val log = system.log

  val exceptionHandler = ExceptionHandler {
    case x => {
      log.error("Internal server error, caused by {}", x)
      ErrorMessage("Internal NRS API error").completeAsJson(500)
    }
  }

  lazy val serviceRoutes: Route =
    handleExceptions(exceptionHandler) {
      pathPrefix(config.appName) {
        path("template" / Segment / "signed-pdf") { case templateId =>
          post {
            optionalHeaderValueByName(ApiKeyHeader) { apiKey =>
              //TODO: replace it
              entity(as[String]) { entity =>

                (for {
                  key <- apiKey.validate()
                  template <- findPdfDocumentTemplate(ClientTemplate(key, templateId))
                  payload <- Some(Payload(entity, template.schema)).validate()
                  pdf <- createPdfDocument(GeneratePdfRequest(payload, template))
                } yield pdf).fold[StandardRoute](
                  err => {
                    log.warn(err.head.error.message)
                    err.map(_.error).completeAsJson(err.head.code)
                  },
                  response => {
                    log.info("PDF document '{}' generated", response.hash)
                    complete {
                      HttpResponse(
                        status = StatusCodes.OK,
                        entity = HttpEntity(ContentTypes.`application/octet-stream`, response.pdf)
                      )
                    }
                  })
              }
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
}
