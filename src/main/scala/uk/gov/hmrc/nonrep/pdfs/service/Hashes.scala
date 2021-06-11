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

import java.math.BigInteger
import java.security.MessageDigest

import uk.gov.hmrc.nonrep.pdfs.model.PayloadWithSchema

trait HashCalculator[A] {
  def calculateHash(value: A): Hash
}

object HashCalculator {
  def apply[A](implicit service: HashCalculator[A]) = service

  object ops {

    implicit class HashCalculatorOps[A: HashCalculator](value: A) {
      def calculateHash()(implicit service: HashCalculator[A]) = service.calculateHash(value)
    }

  }

  implicit val apiKeyHashCalculator: HashCalculator[ApiKey] = (value: ApiKey) => {
    val hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes("UTF-8"))
    String.format("%032x", new BigInteger(1, hash))
  }

  implicit val payloadHashCalculator: HashCalculator[PayloadWithSchema] = (value: PayloadWithSchema) => {
    val hash = MessageDigest.getInstance("SHA-256").digest(value.payload.getBytes("UTF-8"))
    String.format("%032x", new BigInteger(1, hash))
  }

}
