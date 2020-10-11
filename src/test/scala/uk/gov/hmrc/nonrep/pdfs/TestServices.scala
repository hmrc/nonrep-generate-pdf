package uk.gov.hmrc.nonrep.pdfs

import java.io.File
import java.nio.file.Files

object TestServices {

  val apiKey = "dCjF1AwSbqYqxRzfMWHzmIoFc5x2IjrR"

  lazy val sampleRequest_0_6_0 = Files.
    readAllBytes(new File(getClass.getClassLoader.getResource("1584_sample_0.6.0.json").getFile).toPath())

  lazy val sampleRequest_0_7_0 = Files.
    readAllBytes(new File(getClass.getClassLoader.getResource("1584_sample_0.7.0.json").getFile).toPath())
}
