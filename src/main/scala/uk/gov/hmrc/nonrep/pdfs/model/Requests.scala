package uk.gov.hmrc.nonrep.pdfs
package model

case class DocumentTemplate(id: TemplateId, schema: JSONSchema, template: PdfTemplate, profile: PAdESProfile)

case class IncomingRequest(template: TemplateId, payload: Payload, key: Option[ApiKey])

case class AcceptedRequest(template: TemplateId, payload: Payload, key: ApiKey)

case class ValidRequest(template: DocumentTemplate, payload: Payload)

case class PayloadSchema(payload: Payload, schema: JSONSchema)

case class ValidatedDocument(payload: PayloadSchema, template: DocumentTemplate)

case class UnsignedPdfDocument(transactionId: TransactionID, profile: PAdESProfile, pdf: PdfDocument)

case class SignedPdfDocument(pdf: PdfDocument, transactionId: TransactionID, profile: PAdESProfile)