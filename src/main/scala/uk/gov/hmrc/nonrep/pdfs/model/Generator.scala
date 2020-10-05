package uk.gov.hmrc.nonrep.pdfs
package model

import io.circe.Json

case class IncomingRequest(key: ApiKey, payload: Json)

case class GeneratePdfRequest(hash: PayloadHash, template: Template)

case class GeneratePdfResponse(hash: PayloadHash, pdf: Option[PdfDocument])

case class SignPdfRequest(profile: PAdESProfile, pdf: PdfDocument, transactionId: Option[TransactionID])

case class SignPdfResponse(pdf: Option[PdfDocument], transactionId: Option[TransactionID])