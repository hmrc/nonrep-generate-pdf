package uk.gov.hmrc.nonrep.pdfs
package service

import uk.gov.hmrc.nonrep.pdfs.model.{ClientTemplate, GeneratePdfRequest, GeneratePdfResponse, Template}
import uk.gov.hmrc.nonrep.pdfs.server.ServiceConfig

object Documents {
  import Converters._
  import HashCalculator._
  import HashCalculator.ops._

  def createPdfDocument(request: GeneratePdfRequest): EitherNelErr[GeneratePdfResponse] =
    Right(GeneratePdfResponse(request.payload.incomingData.calculateHash(), Array[Byte]()))

  def findPdfDocumentTemplate(template: ClientTemplate)(implicit config: ServiceConfig): EitherNelErr[Template] =
    config.templates.get(template.key).flatMap(_.find(_.id == template.id)).toEitherNel(404, s"Unknown template '$template'")

}
