package uk.gov.hmrc.nonrep.pdfs
package utils

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

object JsonFormats {
  implicit val errorMessageEncoder: Encoder[ErrorMessage] = deriveEncoder
  implicit val buildVersionEncoder: Encoder[BuildVersion] = deriveEncoder
}
