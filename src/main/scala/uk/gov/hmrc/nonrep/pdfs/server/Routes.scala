package uk.gov.hmrc.nonrep.pdfs.server

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete

case class Routes()(implicit val system: ActorSystem[_]){

  val log = system.log

  lazy val serviceRoutes: Route =
    pathPrefix("generate-pdf") {

      pathPrefix("ping") {
        get {
          complete(HttpResponse(StatusCodes.OK, entity = "pong"))
        }
      } ~ pathPrefix("version") {
        pathEndOrSingleSlash {
          get {
            complete(HttpResponse(StatusCodes.OK, entity = "{\"version\" = \"0.0.1\"}"))
          }
        }
      }
    } ~ pathPrefix("ping") {
      get {
        complete(HttpResponse(StatusCodes.OK, entity = "pong"))
      }
    }

}
