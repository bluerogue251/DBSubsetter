package trw.dbsubsetter.akkastreams

import akka.stream._
import akka.stream.stage._
import trw.dbsubsetter.queue.TransformingQueue


// Partially adapted from https://github.com/torodb/akka-chronicle-queue
private[akkastreams] final class TransformingQueueBackedBufferFlow[T, U](backingQueue: TransformingQueue[T, U]) extends GraphStage[FlowShape[T, U]] {

  private[this] val in: Inlet[T] = Inlet.create[T]("TransformingQueueBackedBufferFlow.in")

  private[this] val out: Outlet[U] = Outlet.create[U]("TransformingQueueBackedBufferFlow.out")

  override val shape: FlowShape[T, U] = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

    setHandler(in, new InHandler {
      override def onPush(): Unit = {
        val inputElement: T = grab(in)
        backingQueue.enqueue(inputElement)
        if (isAvailable(out)) doPull()
        pull(in)
      }

      override def onUpstreamFinish(): Unit = {
        if (backingQueue.isEmpty()) {
          completeStage()
        }
      }
    })

    setHandler(out, new OutHandler {
      override def onPull(): Unit = {
        doPull()

        if (isClosed(in) && backingQueue.isEmpty()) {
          completeStage()
        }
      }
    })

    override def preStart(): Unit = {
      pull(in)
    }

    private[this] def doPull(): Unit = {
      val optionalOutputElement: Option[U] = backingQueue.dequeue()
      optionalOutputElement.foreach(outputElement => push[U](out, outputElement))
    }
  }
}
