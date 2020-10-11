package uk.gov.hmrc.nonrep.pdfs
package server

import com.typesafe.config.ConfigFactory
import uk.gov.hmrc.nonrep.pdfs.model.Template

import scala.jdk.CollectionConverters._

class ServiceConfig(val defaultPort: Int = 8000) {

  val appName = "generate-pdf"
  val env: String = sys.env.get("ENV").getOrElse("local")
  val servicePort: Int = sys.env.get("REST_PORT").map(_.toInt).getOrElse(defaultPort)

  private val confFilename = Seq(s"application-$env.conf", "application.conf").
    filter(getClass.getClassLoader.getResource(_) != null).
    head

  private val conf = ConfigFactory.load(confFilename)

  val templates: Map[ApiKey, Seq[Template]] = conf.getConfigList(s"$appName.templates").asScala.map{
    temp => (temp.getString("api-key"), Template(
      temp.getString("template-id"),
      temp.getString("json-schema"),
      Array[Byte](),//TODO: update when iText DITO templates are available
      temp.getString("signing-profile")))
  }.foldLeft(Map[ApiKey, Seq[Template]]()) { case (map, (key, template)) =>
    map.get(key) match {
      case None => map + (key -> Seq(template))
      case Some(seq) => map + (key -> (seq :+ template))
    }
  }

  override def toString =
    s"""
    application name: $appName
    port: $servicePort
    env: $env
    configuration filename: $confFilename
    service templates: $templates"""

}
