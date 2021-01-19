package uk.gov.hmrc.nonrep.pdfs
package server

import java.net.URI

import com.typesafe.config.ConfigFactory
import uk.gov.hmrc.nonrep.pdfs.model.DocumentTemplate
import uk.gov.hmrc.nonrep.pdfs.service.LicenseManager
import uk.gov.hmrc.nonrep.pdfs.utils.FileLoader._

import scala.io.{Codec, Source}
import scala.jdk.CollectionConverters._

class ServiceConfig(val defaultPort: Int = 8000) {

  val appName = "generate-pdf"
  val env: String = sys.env.get("ENV").getOrElse("local")
  val servicePort: Int = sys.env.get("REST_PORT").map(_.toInt).getOrElse(defaultPort)
  val licenseInfo = LicenseManager.useLicense(appName, sys.env.get("DITO_LICENSE"))
  val licenseTrueUpBucket = sys.env.get("DITO_LICENSE_BUCKET").getOrElse("non-repudiation-pdf-generation-usage")

  private val confFilename = Seq(s"application-$env.conf", "application.conf").
    filter(getClass.getClassLoader.getResource(_) != null).
    head

  private val conf = ConfigFactory.load(confFilename)

  val templates: Map[ApiKey, Seq[DocumentTemplate]] = conf.getConfigList(s"$appName.templates").asScala.map {
    temp =>
      (temp.getString("api-key"),
        DocumentTemplate(temp.getString("template-id"),
          temp.getString("template-name"),
          temp.getString("json-schema"),
          loadFile(temp.getString("pdf-template")),
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
  private[pdfs] def loadJsonTemplate(name: JSONSchema) = Source.fromResource(name)(Codec.UTF8).mkString

  override def toString =
    s"""
    application name: $appName
    port: $servicePort
    env: $env
    configuration filename: $confFilename
    service templates: $templates
    license info: $licenseInfo
    licenseTrueUpBucket: $licenseTrueUpBucket"""

}
