package uk.gov.hmrc.nonrep.pdfs
package server

import java.net.URI

import com.typesafe.config.ConfigFactory
import uk.gov.hmrc.nonrep.pdfs.model.DocumentTemplate

import scala.io.Source
import scala.jdk.CollectionConverters._

class ServiceConfig(val defaultPort: Int = 8000) {

  val appName = "generate-pdf"
  val env: String = sys.env.get("ENV").getOrElse("local")
  val servicePort: Int = sys.env.get("REST_PORT").map(_.toInt).getOrElse(defaultPort)

  private val confFilename = Seq(s"application-$env.conf", "application.conf").
    filter(getClass.getClassLoader.getResource(_) != null).
    head

  private val conf = ConfigFactory.load(confFilename)

  val templates: Map[ApiKey, Seq[DocumentTemplate]] = conf.getConfigList(s"$appName.templates").asScala.map {
    temp =>
      (temp.getString("api-key"),
        DocumentTemplate(temp.getString("template-id"),
          temp.getString("json-schema"),
          Array[Byte](), //TODO: update when iText DITO templates are available
          temp.getString("signing-profile")))
  }.foldLeft(Map[ApiKey, Seq[DocumentTemplate]]()) { case (map, (key, template)) =>
    map.get(key) match {
      case None => map + (key -> Seq(template))
      case Some(seq) => map + (key -> (seq :+ template))
    }
  }

  val signaturesServiceUri = URI.create(conf.getString(s"$appName.signatures-service-url"))
  val isSignaturesServiceSecure = signaturesServiceUri.toURL.getProtocol == "https"
  val signaturesServiceHost = signaturesServiceUri.getHost
  val signaturesServicePort = signaturesServiceUri.getPort

  //this method can be changed when JSON schema is kept on k8s config map
  private[pdfs] def loadJsonTemplate(name: JSONSchema) = Source.fromResource(name).mkString

  override def toString =
    s"""
    application name: $appName
    port: $servicePort
    env: $env
    configuration filename: $confFilename
    service templates: $templates"""

}
