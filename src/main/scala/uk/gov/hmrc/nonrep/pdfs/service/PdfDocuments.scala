package uk.gov.hmrc.nonrep.pdfs
package service

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.text.SimpleDateFormat
import java.util.Calendar

import akka.actor.typed.ActorSystem
import com.itextpdf.dito.sdk.core.data.JsonData
import com.itextpdf.dito.sdk.output.{PdfProducer, PdfProducerProperties}
import uk.gov.hmrc.nonrep.pdfs.model.{AcceptedRequest, DocumentTemplate, UnsignedPdfDocument, ValidatedDocument}
import uk.gov.hmrc.nonrep.pdfs.server.ServiceConfig

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

trait PdfDocumentExtender[A] {
  def extend(request: A)(implicit system: ActorSystem[_], config: ServiceConfig): A
}

object PdfDocumentExtender {
  def apply[A](implicit service: PdfDocumentExtender[A]) = service

  object ops {

    implicit class PdfDocumentExtenderOps[A: PdfDocumentExtender](value: A) {
      def extend()(implicit system: ActorSystem[_], config: ServiceConfig) = PdfDocumentExtender[A].extend(value)
    }

  }

  implicit val dateOfIssueExtender: PdfDocumentExtender[EitherNelErr[ValidatedDocument]] = new PdfDocumentExtender[EitherNelErr[ValidatedDocument]]() {
    val formatter = new SimpleDateFormat("dd/MM/yyyy")
    override def extend(request: EitherNelErr[ValidatedDocument])(implicit system: ActorSystem[_], config: ServiceConfig): EitherNelErr[ValidatedDocument] = {
      import Converters._
      import io.circe.parser._
      import io.circe.syntax._

      for {
        doc <- request
        parsed <- parse(doc.payloadWithSchema.payload).toEitherNel(400)
      } yield doc.copy(payloadWithSchema = doc.payloadWithSchema.copy(payload = parsed.deepMerge(Map("creationDate" -> formatter.format(Calendar.getInstance().getTime)).asJson).noSpaces))
    }
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
      val templateName = "api-584-v1.0.0"
      val result = PdfProducer.convertTemplateFromPackage(template, templateName, output, new JsonData(request.payloadWithSchema.payload), new PdfProducerProperties())
      system.log.info(s"PDF generation result ${result.toString}")
      UnsignedPdfDocument(request.payloadWithSchema.payload.calculateHash(), request.template.profile, output.toByteArray)
    }
  }
}