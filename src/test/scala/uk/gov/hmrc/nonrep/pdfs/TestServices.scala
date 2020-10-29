package uk.gov.hmrc.nonrep.pdfs

import java.io.File
import java.nio.file.Files

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.Flow
import uk.gov.hmrc.nonrep.pdfs.model.{SignedPdfDocument, UnsignedPdfDocument}
import uk.gov.hmrc.nonrep.pdfs.server.ServiceConfig
import uk.gov.hmrc.nonrep.pdfs.service.{HashCalculator, ServiceConnector}
import uk.gov.hmrc.nonrep.pdfs.streams.Flows

import scala.util.Try

object TestServices {

  import HashCalculator._
  import HashCalculator.ops._

  lazy val testKit = ActorTestKit()

  implicit def typedSystem = testKit.system

  implicit val config: ServiceConfig = new ServiceConfig()

  val apiKey = "dCjF1AwSbqYqxRzfMWHzmIoFc5x2IjrR"
  val apiKeyHash = apiKey.calculateHash()

  lazy val sampleRequest_0_6_0 = Files.
    readAllBytes(new File(getClass.getClassLoader.getResource("1584_sample_0.6.0.json").getFile).toPath())

  lazy val sampleRequest_0_7_0 = Files.
    readAllBytes(new File(getClass.getClassLoader.getResource("1584_sample_0.7.0.json").getFile).toPath())

  lazy val sampleRequest_1_0_0 = Files.
    readAllBytes(new File(getClass.getClassLoader.getResource("1584_sample_1.0.0.json").getFile).toPath())

  implicit val successfulDigitalSignaturesConnection: ServiceConnector[UnsignedPdfDocument] = new ServiceConnector[UnsignedPdfDocument]() {
    override def request(value: UnsignedPdfDocument)(implicit system: ActorSystem[_], config: ServiceConfig): HttpRequest =
      ServiceConnector.defaultSignaturesServiceConnector.request(value)(system, config)

    override def connectionPool()(implicit system: ActorSystem[_], config: ServiceConfig): ServiceCall[UnsignedPdfDocument] =
      Flow[(HttpRequest, EitherNelErr[UnsignedPdfDocument])].map {
        case (_, sr) => (Try(HttpResponse()), sr)
      }
  }

  val testFlows = new {
    override val signedPdfDocument = Flow[(Try[HttpResponse], EitherNelErr[UnsignedPdfDocument])].map {
      case (_, request) => request.map(x => SignedPdfDocument(x.pdf, x.transactionId, x.profile))
    }
  } with Flows()

}
