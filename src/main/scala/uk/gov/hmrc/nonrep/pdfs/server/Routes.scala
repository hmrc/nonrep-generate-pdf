package uk.gov.hmrc.nonrep.pdfs
package server

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.{ExceptionHandler, Route, StandardRoute}
import akka.stream.Attributes
import akka.stream.scaladsl.{Keep, Sink}
import fr.davit.akka.http.metrics.core.scaladsl.server.HttpMetricsDirectives._
import fr.davit.akka.http.metrics.prometheus.marshalling.PrometheusMarshallers._
import uk.gov.hmrc.nonrep.BuildInfo
import uk.gov.hmrc.nonrep.pdfs.model.{ApiKeyHeader, HeadersConversion, IncomingRequest}
import uk.gov.hmrc.nonrep.pdfs.streams.Flows
import uk.gov.hmrc.nonrep.pdfs.utils.JsonFormats
import uk.gov.hmrc.nonrep.pdfs.metrics.Prometheus._

object Routes {
  def apply(flow: Flows)(implicit system: ActorSystem[_], config: ServiceConfig) = new Routes(flow)
}

class Routes(flow: Flows)(implicit val system: ActorSystem[_], config: ServiceConfig) {

  import HeadersConversion._
  import JsonFormats._
  import JsonResponseService.ops._

  val log = system.log

  val exceptionHandler = ExceptionHandler {
    case x => {
      log.error("Internal server error", x)
      ErrorMessage("Internal NRS API error").completeAsJson(500)
    }
  }

  lazy val serviceRoutes: Route =
    handleExceptions(exceptionHandler) {
      pathPrefix(config.appName) {

        pathLabeled("template" / Segment / "signed-pdf", "signed-pdf") { case templateId =>
          post {
            optionalHeaderValueByName(ApiKeyHeader) { apiKey =>

              (extractDataBytes & extractMaterializer) { (bytes, mat) =>
                val result = bytes.
                  log(name = "flow").
                  addAttributes(Attributes.logLevels(onElement = Attributes.LogLevels.Off, onFinish = Attributes.LogLevels.Info, onFailure = Attributes.LogLevels.Error)).
                  via(flow.materialize).
                  map(IncomingRequest(templateId, _, apiKey)).
                  via(flow.validateApiKey).
                  via(flow.findPdfDocumentTemplate).
                  via(flow.validatePayloadWithJsonSchema).
                  via(flow.addDateOfIssue).
                  via(flow.createPdfDocument).
                  via(flow.signPdfDocument).
                  toMat(Sink.head)(Keep.right).
                  run()(mat)

                onComplete(result) {

                  case scala.util.Success(result) => {
                    result.fold[StandardRoute](
                      err => {
                        log.warn(err.head.error.message)
                        err.map(_.error).completeAsJson(err.head.code)
                      },
                      response => {
                        log.info("PDF document '{}' generated and signed with '{}'", response.transactionId, response.profile)
                        complete {
                          HttpResponse(
                            status = StatusCodes.OK,
                            entity = HttpEntity(ContentTypes.`application/octet-stream`, response.pdf)
                          )
                        }
                      }
                    )
                  }

                  case scala.util.Failure(x) => {
                    log.error("Internal service error", x)
                    ErrorMessage(x.getMessage).completeAsJson(StatusCodes.InternalServerError)
                  }
                }
              }
            }
          }
        } ~ pathLabeled("ping") {
          get {
            complete(HttpResponse(StatusCodes.OK, entity = "pong"))
          }
        } ~ pathLabeled("version") {
          pathEndOrSingleSlash {
            get {
              BuildVersion(version = BuildInfo.version).completeAsJson(StatusCodes.OK)
            }
          }
        }
      } ~ pathLabeled("ping") {
        get {
          complete(HttpResponse(StatusCodes.OK, entity = "pong"))
        }
      } ~ pathLabeled("metrics") {
        get {
          metrics(registry)
        }
      }

    }
}
