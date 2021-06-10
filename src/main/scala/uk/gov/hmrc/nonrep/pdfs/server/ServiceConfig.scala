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

  private val configFile = new java.io.File(s"/etc/config/CONFIG_FILE")

  val config = if(configFile.exists()) {
    ConfigFactory.parseFile(configFile)
  } else {
    ConfigFactory.load("application.conf")
  }

  val templates: Map[ApiKey, Seq[DocumentTemplate]] = config.getConfigList(s"$appName.templates").asScala.map {
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

  val licenseTrueUpBucket = config.getString(s"$appName.license-true-up-bucket")

  val signaturesServiceUri = URI.create(config.getString(s"$appName.signatures-service-url"))
  val isSignaturesServiceSecure = signaturesServiceUri.toURL.getProtocol == "https"
  val signaturesServiceHost = signaturesServiceUri.getHost
  val signaturesServicePort = signaturesServiceUri.getPort

  //this method can be changed when JSON schema is kept on k8s config map
  private[pdfs] def loadJsonTemplate(name: JSONSchema) = Source.fromResource(name)(Codec.UTF8).mkString

  override def toString =
    s"""
    appName: $appName
    port: $servicePort
    env: $env
    service templates: $templates
    license info: $licenseInfo
    licenseTrueUpBucket: $licenseTrueUpBucket
    configFile: ${config.toString}"""

}
