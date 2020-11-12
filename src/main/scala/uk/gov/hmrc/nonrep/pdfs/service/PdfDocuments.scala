package uk.gov.hmrc.nonrep.pdfs
package service

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import akka.actor.typed.ActorSystem
import com.itextpdf.dito.sdk.core.data.JsonData
import com.itextpdf.dito.sdk.output.{PdfProducer, PdfProducerProperties}
import uk.gov.hmrc.nonrep.pdfs.model.{AcceptedRequest, DocumentTemplate, UnsignedPdfDocument, ValidatedDocument}
import uk.gov.hmrc.nonrep.pdfs.server.ServiceConfig

import scala.jdk.CollectionConverters._

trait PdfDocumentTemplate[A] {
  def find(request: A)(implicit system: ActorSystem[_], config: ServiceConfig): Option[DocumentTemplate]
}

object PdfDocumentTemplate {
  def apply[A](implicit service: PdfDocumentTemplate[A]) = service

  object ops {

    implicit class PdfDocumentTemplateOps[A: PdfDocumentTemplate](value: A) {
      def find()(implicit system: ActorSystem[_], config: ServiceConfig) = PdfDocumentTemplate[A].find(value)
    }

  }

  implicit val defaultTemplateFinder: PdfDocumentTemplate[AcceptedRequest] = new PdfDocumentTemplate[AcceptedRequest]() {
    override def find(request: AcceptedRequest)(implicit system: ActorSystem[_], config: ServiceConfig): Option[DocumentTemplate] =
      config.templates.get(request.key).flatMap(_.find(_.id == request.template))
  }

}

trait PdfDocumentGenerator[A] {
  def create(request: A)(implicit system: ActorSystem[_], config: ServiceConfig): UnsignedPdfDocument
}

object PdfDocumentGenerator {
  def apply[A](implicit service: PdfDocumentGenerator[A]) = service

  object ops {

    implicit class PdfDocumentGeneratorOps[A: PdfDocumentGenerator](value: A) {
      def create()(implicit system: ActorSystem[_], config: ServiceConfig) = PdfDocumentGenerator[A].create(value)
    }

  }

  implicit val defaultPdfGenerator: PdfDocumentGenerator[ValidatedDocument] = new PdfDocumentGenerator[ValidatedDocument]() {
    override def create(request: ValidatedDocument)(implicit system: ActorSystem[_], config: ServiceConfig): UnsignedPdfDocument = {
      import HashCalculator._
      import HashCalculator.ops._

      val template = new ByteArrayInputStream(request.template.template)
      val output = new ByteArrayOutputStream()
      val result = PdfProducer.convertTemplateFromPackage(template, "output", output, new JsonData(request.payload.payload), new PdfProducerProperties())
      system.log.info(s"PDF generation result ${result.toString}")
      UnsignedPdfDocument(request.payload.payload.calculateHash(), request.template.profile, output.toByteArray)
    }
  }
}