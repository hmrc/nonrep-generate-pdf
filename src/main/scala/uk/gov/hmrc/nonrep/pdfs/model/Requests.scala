package uk.gov.hmrc.nonrep.pdfs.model


sealed trait RequestHeader {
  def name: String
}

case object ApiKeyHeader extends RequestHeader {
  val name = "x-api-key"
}

object HeadersConversion {
  import scala.language.implicitConversions

  implicit def convertRequestHeader(header: RequestHeader) = header.name
}
