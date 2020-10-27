package uk.gov.hmrc.nonrep.pdfs
package streams

import akka.stream._
import akka.stream.stage._
import uk.gov.hmrc.nonrep.pdfs.service.Converters._

object EitherStage {
  def apply[A0, A1 <: EitherNelErr[A0], A2]: EitherStage[A0, A1, A2] = new EitherStage[A0, A1, A2]()
}

final class EitherStage[A0, A1 <: EitherNelErr[A0], A2] extends GraphStage[FanInShape2[A1, A2, EitherNelErr[A2]]] {

  override def initialAttributes = Attributes.name("EitherStage")

  override val shape: FanInShape2[A1, A2, EitherNelErr[A2]] = new FanInShape2[A1, A2, EitherNelErr[A2]]("EitherStage")
  private val left = shape.in0
  private val right = shape.in1
  private val out = shape.out

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) with StageLogging {
    var pending = 0
    var willShutDown = false
    var wasPulled = false
    var obj: Option[EitherNelErr[A2]] = None


    private def pushLeft(): Unit = {
      if (isAvailable(left)) {
        val in = grab(left)
        obj = Some(in.withRight[A2])
      }
      if (wasPulled) {
        obj.foreach(push(out, _))
        obj = None
      }
    }

    private def pushRight(): Unit = {
      if (isAvailable(right)) {
        val in = grab(right)
        obj = Some(Right(in))
      }
      if (wasPulled) {
        obj.foreach(push(out, _))
        obj = None
      }
    }

    private def pullLeft(): Unit = {
      if (!hasBeenPulled(left)) tryPull(left)
    }

    private def pullRight(): Unit = {
      if (!hasBeenPulled(right)) tryPull(right)
    }

    private def pushAll(): Unit = {
      pushLeft()
      pushRight()
      if (willShutDown)
        completeStage()
      else {
        pullLeft()
        pullRight()
      }
    }

    override def preStart(): Unit = {
      pull(right)
      pull(left)
    }

    setHandler(left, new InHandler {
      override def onPush(): Unit = {
        pending += 1
        pushAll()
      }

      override def onUpstreamFinish(): Unit = {
        cancel(left)
        willShutDown = true
      }

    })
    setHandler(right, new InHandler {
      override def onPush(): Unit = {
        pending += 1
        pushAll()
      }

      override def onUpstreamFinish(): Unit = {
        cancel(right)
        willShutDown = true
      }
    })

    setHandler(out, new OutHandler {
      override def onPull(): Unit = {
        wasPulled = true
        if (pending > 0) pushAll()
        pending = 0
        pushLeft()
        pushRight()
        pullLeft()
        pullRight()
      }
    })
  }

  override def toString = "EitherStage"
}
