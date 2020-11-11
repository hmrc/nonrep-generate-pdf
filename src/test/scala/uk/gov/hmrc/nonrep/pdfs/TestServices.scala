package uk.gov.hmrc.nonrep.pdfs

import java.io.File
import java.nio.charset.Charset
import java.nio.file.{Files, Paths}

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.Flow
import uk.gov.hmrc.nonrep.pdfs.model.{SignedPdfDocument, UnsignedPdfDocument, ValidatedDocument}
import uk.gov.hmrc.nonrep.pdfs.server.ServiceConfig
import uk.gov.hmrc.nonrep.pdfs.service.{HashCalculator, PdfDocumentGenerator, ServiceConnector}
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

  val sampleRequests = Map(
    "trusts-5mld-1-0-0" -> Files.readAllBytes(new File(getClass.getClassLoader.getResource("1584_sample_1.0.0.json").getFile).toPath()),
    "trusts-5mld-0-7-0" -> Files.readAllBytes(new File(getClass.getClassLoader.getResource("1584_sample_0.7.0.json").getFile).toPath()),
    "trusts-5mld-0-6-0" -> Files.readAllBytes(new File(getClass.getClassLoader.getResource("1584_sample_0.6.0.json").getFile).toPath())
  )

  def sampleRequestPayload(templateId: String): String =
    sampleRequests.view.filterKeys(_ == templateId).mapValues(new String(_, Charset.forName("utf-8"))).values.head

  implicit val successfulDigitalSignaturesConnection: ServiceConnector[UnsignedPdfDocument] = new ServiceConnector[UnsignedPdfDocument]() {
    override def request(value: UnsignedPdfDocument)(implicit system: ActorSystem[_], config: ServiceConfig): HttpRequest =
      ServiceConnector.defaultSignaturesServiceConnector.request(value)(system, config)

    override def connectionPool()(implicit system: ActorSystem[_], config: ServiceConfig): ServiceCall[UnsignedPdfDocument] =
      Flow[(HttpRequest, EitherNelErr[UnsignedPdfDocument])].map {
        case (_, sr) => (Try(HttpResponse()), sr)
      }
  }

  implicit val testPdfGenerator: PdfDocumentGenerator[ValidatedDocument] = new PdfDocumentGenerator[ValidatedDocument]() {
    override def create(request: ValidatedDocument)(implicit system: ActorSystem[_], config: ServiceConfig): UnsignedPdfDocument = {
      val uri = getClass.getClassLoader.getResource("2020-06-26-trusts_sample.pdf").toURI
      val doc = Files.readAllBytes(Paths.get(uri))
      UnsignedPdfDocument(request.payload.payload.calculateHash(), request.template.profile, doc)
    }
  }

  val testFlows = new {
    override val signedPdfDocument = Flow[(Try[HttpResponse], EitherNelErr[UnsignedPdfDocument])].map {
      case (_, request) => request.map(x => SignedPdfDocument(x.pdf, x.transactionId, x.profile))
    }
  } with Flows()

}
