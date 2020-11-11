package uk.gov.hmrc.nonrep.pdfs
package utils

import java.nio.file.{FileSystems, Files, Paths}

import scala.jdk.CollectionConverters._
import scala.util.Try

object FileLoader {

  def loadFile(name: String) = {
    val uri = getClass.getClassLoader.getResource(name).toURI
    Try(FileSystems.newFileSystem(uri, Map[String, Any]().asJava))
    Files.readAllBytes(Paths.get(uri))
  }

}
