/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.nonrep.pdfs
package model

case class DocumentTemplate(id: TemplateId, name: TemplateName, schema: JSONSchema, template: PdfTemplate, profile: PAdESProfile)

case class IncomingRequest(template: TemplateId, payload: Payload, key: Option[ApiKey])

case class AcceptedRequest(template: TemplateId, payload: Payload, key: ApiKey)

case class ValidRequest(template: DocumentTemplate, payload: Payload)

case class PayloadWithSchema(payload: Payload, schema: JSONSchema)

case class ValidatedDocument(payloadWithSchema: PayloadWithSchema, template: DocumentTemplate)

case class UnsignedPdfDocument(transactionId: TransactionID, profile: PAdESProfile, pdf: PdfDocument, pageCount: Int)

case class SignedPdfDocument(pdf: PdfDocument, transactionId: TransactionID, profile: PAdESProfile)