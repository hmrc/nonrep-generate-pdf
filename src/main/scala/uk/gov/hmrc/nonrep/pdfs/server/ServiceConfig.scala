package uk.gov.hmrc.nonrep.pdfs
package server

import uk.gov.hmrc.nonrep.pdfs.model.Template

class ServiceConfig(val defaultPort: Int = 8000) {

  val appName = "generate-pdf"

  val servicePort: Int = sys.env.get("REST_PORT").map(_.toInt).getOrElse(defaultPort)

  //TODO: it'll be re-implemented together with akka config
  val templates: Map[ApiKey, Seq[Template]] = Map("interim" -> Seq(Template("interim", "interim", Array[Byte](), "interim")))


}
