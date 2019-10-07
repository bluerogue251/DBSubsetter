package trw.dbsubsetter.akkastreams

import akka.event.Logging
import akka.stream._
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}

/*
 * A flow which always backpressures. To be used for debugging purposes only.
 */
final class BackpressureFlow[T] extends GraphStage[FlowShape[T, T]] {

  val in = Inlet[T](Logging.simpleName(this) + ".in")
  val out = Outlet[T](Logging.simpleName(this) + ".out")
  override val shape = FlowShape(in, out): FlowShape[T, T]

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) with InHandler with OutHandler {
    override def preStart(): Unit = {
      pull(in)
    }

    override def onPush(): Unit = {
      grab(in)
    }

    override def onPull(): Unit = {} // No-op

    override def onUpstreamFinish(): Unit = {
      completeStage()
    }

    setHandlers(in, out, this)
  }

}