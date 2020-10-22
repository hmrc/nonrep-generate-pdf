package uk.gov.hmrc.nonrep.pdfs
package model

case class Payload(incomingData: String, schema: JSONSchema)

case class Template(id: TemplateId, schema: JSONSchema, template: PdfTemplate, profile: PAdESProfile)

case class IncomingRequest(template: TemplateId, payload: String, key: Option[ApiKey])

case class AcceptedRequest(template: TemplateId, payload: String, key: ApiKey)

case class ValidatedRequest(template: Template, payload: String)

case class GeneratePdfDocument(payload: Payload, template: Template)

case class SignPdfDocument(transactionId: TransactionID, profile: PAdESProfile, pdf: PdfDocument)

case class SignedPdfDocument(pdf: PdfDocument, transactionId: TransactionID)