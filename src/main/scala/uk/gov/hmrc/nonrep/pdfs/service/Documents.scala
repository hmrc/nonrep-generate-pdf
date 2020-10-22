package uk.gov.hmrc.nonrep.pdfs
package service

import java.nio.file.spi.FileSystemProvider
import java.nio.file.{FileSystems, Files, Paths}

import uk.gov.hmrc.nonrep.pdfs.model.{AcceptedRequest, GeneratePdfDocument, SignPdfDocument, Template}
import uk.gov.hmrc.nonrep.pdfs.server.ServiceConfig

import scala.jdk.CollectionConverters._
import scala.util.Try

object Documents {
  import Converters._
  import HashCalculator._
  import HashCalculator.ops._

  def createPdfDocument(request: GeneratePdfDocument): EitherNelErr[SignPdfDocument] =
    Right(SignPdfDocument(request.payload.incomingData.calculateHash(), request.template.profile, Array[Byte]()))

  def createPdfDocument(request: EitherNelErr[GeneratePdfDocument]): EitherNelErr[SignPdfDocument] = {
    val uri = getClass.getClassLoader.getResource("2020-06-26-trusts_sample.pdf").toURI
    Try(FileSystems.newFileSystem(uri, Map[String, Any]().asJava))
    val doc = Files.readAllBytes(Paths.get(uri))
    request.map(x => SignPdfDocument(x.payload.incomingData.calculateHash(), x.template.profile, doc))
  }

  def findPdfDocumentTemplate(request: EitherNelErr[AcceptedRequest])(implicit config: ServiceConfig): EitherNelErr[Template] =
    request.flatMap(req => config.templates.get(req.key).flatMap(_.find(_.id == req.template)).toEitherNel(404, s"Unknown template '$req.template'"))
}
