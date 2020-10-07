package uk.gov.hmrc.nonrep.pdfs

import java.io.File
import java.nio.file.Files

object TestServices {

  lazy val testPayload = Files.
    readAllBytes(new File(getClass.getClassLoader.getResource("sample.request.0.6.0.json").getFile).toPath())

}
