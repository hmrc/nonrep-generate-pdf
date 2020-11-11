package uk.gov.hmrc.nonrep.pdfs
package streams

import java.nio.charset.Charset
import java.util.UUID

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import akka.util.ByteString
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.nonrep.pdfs.model._
import uk.gov.hmrc.nonrep.pdfs.server.ServiceConfig

class FlowsSpec extends AnyWordSpec with ScalatestRouteTest {

  import uk.gov.hmrc.nonrep.pdfs.TestServices._

  lazy val testKit = ActorTestKit()

  implicit def typedSystem = testKit.system

  override def createActorSystem(): akka.actor.ActorSystem = testKit.system.toClassic

  implicit val config: ServiceConfig = new ServiceConfig()

  "Generate PDFs' flow" should {
    val payload = sampleRequestPayload("trusts-5mld-1-0-0")

    "materialize document" in {
      val source = TestSource.probe[ByteString]
      val sink = TestSink.probe[Payload]
      val (pub, sub) = source.via(testFlows.materialize).toMat(sink)(Keep.both).run()
      val input = "test"
      pub.sendNext(ByteString(input)).sendComplete()
      val doc = sub.request(1).expectNext()
      input shouldBe doc
    }

    "validate api key and accept valid key" in {
      val source = TestSource.probe[IncomingRequest]
      val sink = TestSink.probe[EitherNelErr[AcceptedRequest]]
      val (pub, sub) = source.via(testFlows.validateApiKey).toMat(sink)(Keep.both).run()
      val input = IncomingRequest("templateId", "data", Some(apiKey))
      pub.sendNext(input).sendComplete()
      val response = sub.request(1).expectNext()
      response.isRight shouldBe true
    }

    "validate api key and reject invalid key" in {
      val source = TestSource.probe[IncomingRequest]
      val sink = TestSink.probe[EitherNelErr[AcceptedRequest]]
      val (pub, sub) = source.via(testFlows.validateApiKey).toMat(sink)(Keep.both).run()
      val input = IncomingRequest("templateId", "data", Some("apiKey"))
      pub.sendNext(input).sendComplete()
      val response = sub.request(1).expectNext()
      response.isLeft shouldBe true
    }

    "validate api key and reject empty key" in {
      val source = TestSource.probe[IncomingRequest]
      val sink = TestSink.probe[EitherNelErr[AcceptedRequest]]
      val (pub, sub) = source.via(testFlows.validateApiKey).toMat(sink)(Keep.both).run()
      val input = IncomingRequest("templateId", "data", None)
      pub.sendNext(input).sendComplete()
      val response = sub.request(1).expectNext()
      response.isLeft shouldBe true
    }

    "find document template for known template id" in {
      val template = "trusts-5mld-1-0-0"
      val source = TestSource.probe[EitherNelErr[AcceptedRequest]]
      val sink = TestSink.probe[EitherNelErr[ValidRequest]]
      val (pub, sub) = source.via(testFlows.findPdfDocumentTemplate).toMat(sink)(Keep.both).run()
      val input = AcceptedRequest(template, "data", apiKeyHash)
      pub.sendNext(Right(input)).sendComplete()
      val response = sub.request(1).expectNext()
      response.isRight shouldBe true
    }

    "fail on finding document template for unknown template id" in {
      val template = "trusts-5mld-x-x-x"
      val source = TestSource.probe[EitherNelErr[AcceptedRequest]]
      val sink = TestSink.probe[EitherNelErr[ValidRequest]]
      val (pub, sub) = source.via(testFlows.findPdfDocumentTemplate).toMat(sink)(Keep.both).run()
      val input = AcceptedRequest(template, "data", apiKeyHash)
      pub.sendNext(Right(input)).sendComplete()
      val response = sub.request(1).expectNext()
      response.isLeft shouldBe true
    }

    "validate payload with schema" in {
      val template = config.templates(apiKeyHash).find(_.id == "trusts-5mld-1-0-0").get
      val source = TestSource.probe[EitherNelErr[ValidRequest]]
      val sink = TestSink.probe[EitherNelErr[ValidatedDocument]]
      val (pub, sub) = source.via(testFlows.validatePayloadWithJsonSchema).toMat(sink)(Keep.both).run()
      val input = ValidRequest(template, payload)
      pub.sendNext(Right(input)).sendComplete()
      val response = sub.request(1).expectNext()
      response.isRight shouldBe true
    }

    "fail on payload validation with wrong schema" in {
      val template = config.templates(apiKeyHash).find(_.id == "trusts-5mld-0-6-0").get
      val source = TestSource.probe[EitherNelErr[ValidRequest]]
      val sink = TestSink.probe[EitherNelErr[ValidatedDocument]]
      val (pub, sub) = source.via(testFlows.validatePayloadWithJsonSchema).toMat(sink)(Keep.both).run()
      val input = ValidRequest(template, payload)
      pub.sendNext(Right(input)).sendComplete()
      val response = sub.request(1).expectNext()
      response.isLeft shouldBe true
    }

    "create pdf document" in {
      val template = config.templates(apiKeyHash).find(_.id == "trusts-5mld-1-0-0").get
      val source = TestSource.probe[EitherNelErr[ValidatedDocument]]
      val sink = TestSink.probe[EitherNelErr[UnsignedPdfDocument]]
      val (pub, sub) = source.via(testFlows.createPdfDocument).toMat(sink)(Keep.both).run()
      val input = ValidatedDocument(PayloadWithSchema(payload, template.schema), template)
      pub.sendNext(Right(input)).sendComplete()
      val response = sub.request(1).expectNext()
      response.isRight shouldBe true
    }

    "sign pdf document" in {
      val template = config.templates(apiKeyHash).find(_.id == "trusts-5mld-1-0-0").get
      val transactionId = UUID.randomUUID().toString
      val source = TestSource.probe[EitherNelErr[UnsignedPdfDocument]]
      val sink = TestSink.probe[EitherNelErr[SignedPdfDocument]]
      val (pub, sub) = source.via(testFlows.signPdfDocument).toMat(sink)(Keep.both).run()
      val input = UnsignedPdfDocument(transactionId, template.profile, sampleRequests("trusts-5mld-1-0-0"))
      pub.sendNext(Right(input)).sendComplete()
      val response = sub.request(1).expectNext()
      response.isRight shouldBe true
    }
  }
}
