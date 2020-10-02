package uk.gov.hmrc.nonrep.pdfs
package server

import uk.gov.hmrc.nonrep.pdfs.model.Template

class ServiceConfig {

  val templates: Map[ApiKey, Seq[Template]] = ??? //TODO: it'll be implemented together with akka config

}
