package uk.gov.hmrc.nonrep.pdfs
package service

import uk.gov.hmrc.nonrep.pdfs.server.ServiceConfig

object Validators {

  import Converters._

  def validateApiKey(key: Option[ApiKey])(implicit config: ServiceConfig): EitherResponse[ApiKeyHash] = {
    key.map(_.calculateHash).flatMap(hash => config.templates.get(hash).map(_ => hash)).toEitherResponse(401, s"Unknown API key '$key'")
  }
}
