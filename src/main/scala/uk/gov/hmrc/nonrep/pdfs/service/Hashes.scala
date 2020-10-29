package uk.gov.hmrc.nonrep.pdfs
package service

import java.math.BigInteger
import java.security.MessageDigest

import uk.gov.hmrc.nonrep.pdfs.model.PayloadSchema

trait HashCalculator[A] {
  def calculateHash(value: A): Hash
}

object HashCalculator {
  def apply[A]()(implicit service: HashCalculator[A]) = service

  object ops {

    implicit class HashCalculatorOps[A: HashCalculator](value: A) {
      def calculateHash()(implicit service: HashCalculator[A]) = service.calculateHash(value)
    }

  }

  implicit val apiKeyHashCalculator: HashCalculator[ApiKey] = (value: ApiKey) => {
    val hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes("UTF-8"))
    String.format("%032x", new BigInteger(1, hash))
  }

  implicit val payloadHashCalculator: HashCalculator[PayloadSchema] = (value: PayloadSchema) => {
    val hash = MessageDigest.getInstance("SHA-256").digest(value.payload.getBytes("UTF-8"))
    String.format("%032x", new BigInteger(1, hash))
  }

}
