package uk.gov.hmrc.nonrep.pdfs
package utils

import java.io.File
import java.nio.charset.Charset
import java.nio.file.{Files, Paths}
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.security.{Key, KeyFactory, SecureRandom}
import java.util.Base64

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object Crypto {

  val pkCipher = Cipher.getInstance("RSA")

  def getPrivateKey(filename: String) = {
    val keyBytes = Files.readAllBytes(new File(filename).toPath())
    val spec = new PKCS8EncodedKeySpec(keyBytes)
    KeyFactory.getInstance("RSA").generatePrivate(spec)
  }

  def getPublicKey(filename: String) = {
    val keyBytes = Files.readAllBytes(new File(filename).toPath())
    val spec = new X509EncodedKeySpec(keyBytes)
    KeyFactory.getInstance("RSA").generatePublic(spec)
  }

  def getPublicKeyPEM(cert: String) = {
    val publicKeyPEM = cert.
      replace("-----BEGIN PUBLIC KEY-----", "").
      replaceAll(System.lineSeparator(), "").
      replace("-----END PUBLIC KEY-----", "")
    val encoded = Base64.getDecoder.decode(publicKeyPEM)
    val spec = new X509EncodedKeySpec(encoded)
    KeyFactory.getInstance("RSA").generatePublic(spec)
  }

  def encrypt(key: Key, input: Array[Byte]): Array[Byte] = {
    pkCipher.init(Cipher.ENCRYPT_MODE, key)
    pkCipher.doFinal(input)
  }

  def decrypt(key: Key, input: Array[Byte]): Array[Byte] = {
    pkCipher.init(Cipher.DECRYPT_MODE, key)
    pkCipher.doFinal(input)
  }

  def encrypt(pk: Array[Byte], input: Array[Byte]) = {
    val secret = new SecretKeySpec(pk, "AES")
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    cipher.init(Cipher.ENCRYPT_MODE, secret)
    cipher.doFinal(input)
  }

  def decrypt(pk: Array[Byte], input: Array[Byte]) = {
    val secret = new SecretKeySpec(pk, "AES")
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    cipher.init(Cipher.DECRYPT_MODE, secret)
    cipher.doFinal(input)
  }

  def main(args: Array[String]) : Unit = {
    //get this one from PK encoding
    val pkx = "generate-pdf"
    val pk = pkx.foldLeft(Array.fill(32 - pkx.length)("0").mkString){ (a, c) => a + c}.getBytes("utf-8")

    val privateKey = getPrivateKey("/home/greg/1/2/1/private.der")
    //val publicKey = getPublicKey("/home/greg/1/2/1/public.der")
    val key = new String(Files.readAllBytes(new File("/home/greg/1/2/1/key.pub").toPath()), Charset.defaultCharset())
    val publicKey = getPublicKeyPEM(key)

    //val file = new String(Files.readAllBytes(new File("/home/greg/1/2/1/itext.dito.hmrc.lic.xml").toPath()), Charset.forName("utf-8"))

    val encrypted = encrypt(privateKey, pk)
    val encoded = Base64.getEncoder.encodeToString(encrypted)
    println(encoded)
    println(new String(decrypt(publicKey, Base64.getDecoder.decode(encoded))))
    println(pkCipher.getAlgorithm)

    //val test = "test"
    val test= new String(Files.readAllBytes(new File("/home/greg/1/2/1/itext.dito.hmrc.lic.xml").toPath()), Charset.forName("utf-8"))

    val aesEnc = encrypt(pk, test.getBytes("utf-8"))
    println(new String(aesEnc))
    println()
    println(Base64.getEncoder.encodeToString(aesEnc))
    Files.write(Paths.get("/home/greg/1/2/1/itext.dito.hmrc.lic.enc.xml"), Base64.getEncoder.encode(aesEnc))
    println()
    val aesDec = decrypt(pk, aesEnc)
    println(new String(aesDec))

    val license = Base64.getDecoder.decode(Files.readAllBytes(new File("/home/greg/1/2/1/itext.dito.hmrc.lic.enc.xml").toPath()))
    println(new String(decrypt(pk, license)))
  }

}
