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

package uk.gov.hmrc.nonrep.pdfs.streams

import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.FlowShape
import akka.stream.scaladsl.{Flow, GraphDSL, Keep, Partition}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import cats.data.NonEmptyList
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.nonrep.pdfs.service.Converters._
import uk.gov.hmrc.nonrep.pdfs.{EitherNelErr, ErrorResponse}

class GraphsSpec extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  val testStage = EitherStage[String, EitherNelErr[String], String]

  def partitionRequests[A](ports: Int) = Partition[EitherNelErr[A]](ports, _ match {
    case Left(_) => 0
    case Right(_) => 1
  })

  val testFlow = Flow.fromGraph(GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

    val partition = b.add(partitionRequests[String](2))
    val testShape = b.add(testStage)

    partition.out(0) ~> testShape.in0
    partition.out(1).map {
      _ match {
        case Right(v) => v
        case Left(_) => throw new IllegalStateException()
      }
    } ~> testShape.in1
    FlowShape(partition.in, testShape.out)
  })

  "EitherStage component" should {

    "return left for error" in {
      val source = TestSource.probe[EitherNelErr[String]]
      val sink = TestSink.probe[EitherNelErr[String]]
      val (pub, sub) = source.via(testFlow).toMat(sink)(Keep.both).run()
      pub
        .sendNext(Option.empty[String].toEitherNel(500, "error"))
        .sendComplete()
      sub
        .request(1)
        .expectNext(Left(NonEmptyList.one(ErrorResponse(500, "error"))))
        .expectComplete()
    }

    "return right for success" in {
      val source = TestSource.probe[EitherNelErr[String]]
      val sink = TestSink.probe[EitherNelErr[String]]
      val (pub, sub) = source.via(testFlow).toMat(sink)(Keep.both).run()
      pub
        .sendNext(Some("test").toEitherNel(500, "error"))
        .sendComplete()
      sub
        .request(1)
        .expectNext(Right("test"))
        .expectComplete()
    }
  }
}
