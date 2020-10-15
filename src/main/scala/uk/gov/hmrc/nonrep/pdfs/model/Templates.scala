package uk.gov.hmrc.nonrep.pdfs
package model

case class Template(id: TemplateId, schema: JSONSchema, template: PdfTemplate, profile: PAdESProfile)

case class ClientTemplate(key: ApiKey, id: TemplateId)
