package uk.gov.hmrc.nonrep.pdfs
package service

import uk.gov.hmrc.nonrep.pdfs.model.{GeneratePdfRequest, GeneratePdfResponse, Template}
import uk.gov.hmrc.nonrep.pdfs.server.ServiceConfig

object Documents {

  def createPdfDocument(request: GeneratePdfRequest): EitherErr[GeneratePdfResponse] = Right(GeneratePdfResponse(request.hash, Array[Byte]()))

  def findPdfDocumentTemplate(key: ApiKey, template: TemplateId)(implicit config: ServiceConfig): Option[Template] = {
    config.templates(key).find(_.id == template)
  }
}
