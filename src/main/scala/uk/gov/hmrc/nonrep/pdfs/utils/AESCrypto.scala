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

object AESCrypto {

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

}
