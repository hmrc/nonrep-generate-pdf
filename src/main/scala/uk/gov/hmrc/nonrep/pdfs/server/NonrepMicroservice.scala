package uk.gov.hmrc.nonrep.pdfs.server

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http

import scala.util.{Failure, Success}

case class NonrepMicroservice(routes: Routes)(implicit val system: ActorSystem[_], config: ServiceConfig){

  import system.executionContext

  //TODO: update when config is updated
  val serverBinding = Http().newServerAt("0.0.0.0", config.servicePort).bind(routes.serviceRoutes)
  serverBinding.onComplete {
    case Success(binding) =>
      val address = binding.localAddress
      system.log.info("Server '{}' is online at http://{}:{}/ ", "generate-pdf", address.getHostString, address.getPort)
    case Failure(ex) =>
      system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
      system.terminate()
  }

}

object Main {
  /**
   * https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/java-dg-jvm-ttl.html
   */
  java.security.Security.setProperty("networkaddress.cache.ttl" , "60")

  implicit val config: ServiceConfig = new ServiceConfig()

  def main(args: Array[String]) : Unit = {
    val rootBehavior = Behaviors.setup[Nothing] { context =>

      val routes = Routes()(context.system, config)

      NonrepMicroservice(routes)(context.system, config)

      Behaviors.empty
    }
    implicit val system = ActorSystem[Nothing](rootBehavior, s"NrsServer-${config.appName}")

  }
}