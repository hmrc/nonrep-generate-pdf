package uk.gov.hmrc.nonrep.pdfs
package service

import uk.gov.hmrc.nonrep.pdfs.model.{GeneratePdfRequest, GeneratePdfResponse, Template}
import uk.gov.hmrc.nonrep.pdfs.server.ServiceConfig

object Documents {
  import Converters._

  def createPdfDocument(request: GeneratePdfRequest): EitherErr[GeneratePdfResponse] = Right(GeneratePdfResponse(request.hash, Array[Byte]()))

  def findPdfDocumentTemplate(key: ApiKey, template: TemplateId)(implicit config: ServiceConfig): EitherResponse[Template] =
    config.templates.get(key).flatMap(_.find(_.id == template)).toEitherResponse(404, s"Unknown template '$template'")
}
