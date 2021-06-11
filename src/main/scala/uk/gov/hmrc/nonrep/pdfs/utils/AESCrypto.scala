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
