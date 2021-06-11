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
package service

import java.nio.file.Files
import java.util.Base64
import com.itextpdf.dito.sdk.license.DitoLicense
import uk.gov.hmrc.nonrep.pdfs.utils.AESCrypto._

object LicenseManager {
  def useLicense(appName: String, value: Option[String]) = {
    val key = appName.foldLeft(Array.fill(32 - appName.length)("0").mkString){ (a, c) => a + c}.getBytes("utf-8")
    value.
      map(Base64.getDecoder.decode).
      map(decrypt(key, _)).
      map(Files.write(Files.createTempFile("license", "dito"), _)).
      map(_.toFile).
      map(DitoLicense.loadLicenseFile).
      map(info => LicenseInfo(info.getType, info.getExpire, info.getKey, info.getVersion, info.getMeta.getComment))
  }
}
