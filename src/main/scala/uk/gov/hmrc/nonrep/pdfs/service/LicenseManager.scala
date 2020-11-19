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
      map(info => LicenseInfo(info.getType, info.getExpire))
  }
}
