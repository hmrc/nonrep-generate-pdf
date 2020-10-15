package uk.gov.hmrc.nonrep.pdfs
package service

import io.circe.parser._
import io.circe.schema.Schema
import uk.gov.hmrc.nonrep.pdfs.model.Payload
import uk.gov.hmrc.nonrep.pdfs.server.ServiceConfig

import scala.io.Source

trait Validator[A] {
  def validate(data: Option[A])(implicit config: ServiceConfig): EitherNelErr[A]
}

object Validator {

  def apply[A](implicit service: Validator[A]) = service

  object ops {

    implicit class ValidatorOps[A: Validator](data: Option[A]) {
      def validate()(implicit config: ServiceConfig) = Validator[A].validate(data)
    }

  }

  implicit val payloadJsonSchemaValidator: Validator[Payload] = new Validator[Payload]() {

    import Converters._

    override def validate(data: Option[Payload])(implicit config: ServiceConfig): EitherNelErr[Payload] =
      data.
        map(payload => Payload(payload.incomingData, config.loadJsonTemplate(payload.schema))).toEitherNel(404, "Payload cannot be empty").
        flatMap(payload => {
          for {
            schema <- parse(payload.schema).map(Schema.load(_)).toEitherNel(400)
            data <- parse(payload.incomingData).toEitherNel(400)
            result <- schema.validate(data).toEither.left.map(_.map(x => ErrorResponse(400, x.getMessage))).map(_ => payload)
          } yield result
        })
  }

  implicit val apiKeyValidator: Validator[ApiKey] = new Validator[ApiKey]() {

    import Converters._
    import HashCalculator._
    import HashCalculator.ops._

    override def validate(key: Option[ApiKey])(implicit config: ServiceConfig): EitherNelErr[ApiKey] =
      key.map(_.calculateHash()).flatMap(hash => config.templates.get(hash).map(_ => hash)).
        toEitherNel(401, s"Unknown API key '$key'")

  }

}
