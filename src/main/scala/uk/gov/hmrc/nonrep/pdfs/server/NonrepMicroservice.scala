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

package uk.gov.hmrc.nonrep.pdfs.server

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import uk.gov.hmrc.nonrep.pdfs.streams.Flows
import fr.davit.akka.http.metrics.core.HttpMetrics._
import uk.gov.hmrc.nonrep.pdfs.metrics.Prometheus._

import scala.util.{Failure, Success}

case class NonrepMicroservice(routes: Routes)(implicit val system: ActorSystem[_], config: ServiceConfig) {

  import system.executionContext

  val serverBinding = Http().newMeteredServerAt("0.0.0.0", config.servicePort, registry).bind(routes.serviceRoutes)
  serverBinding.onComplete {
    case Success(binding) =>
      val address = binding.localAddress
      system.log.info("Server '{}' is online at http://{}:{}/ with configuration: {}", config.appName, address.getHostString, address.getPort, config.toString)
    case Failure(ex) =>
      system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
      system.terminate()
  }

}

object Main {
  /**
   * https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/java-dg-jvm-ttl.html
   */
  java.security.Security.setProperty("networkaddress.cache.ttl", "60")

  implicit val config: ServiceConfig = new ServiceConfig()

  def main(args: Array[String]): Unit = {
    val rootBehavior = Behaviors.setup[Nothing] { context =>

      val flows = Flows()(context.system, implicitly, implicitly, implicitly, implicitly, implicitly, implicitly, implicitly, implicitly)
      val routes = Routes(flows)(context.system, implicitly)

      NonrepMicroservice(routes)(context.system, implicitly)

      Behaviors.empty
    }
    implicit val system = ActorSystem[Nothing](rootBehavior, s"NrsServer-${config.appName}")

  }
}