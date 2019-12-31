package trw.dbsubsetter.akkastreams

import java.util.concurrent.CompletableFuture

import akka.NotUsed
import akka.stream._
import akka.stream.stage._


/**
  * Partially adapted from https://github.com/torodb/akka-chronicle-queue
  * Refer to akka.stream.impl.fusing.Buffer for onUpstreamFinish logic
  * Relevant docs: https://doc.akka.io/docs/akka/current/stream/stream-customize.html
  */
private[akkastreams] final class QueueBackedBufferSource[T](backingQueue: TransformingQueue[T, _])
  extends GraphStageWithMaterializedValue[SinkShape[T], CompletableFuture[NotUsed]] {

  private[this] val in: Inlet[T] = Inlet.create[T]("QueueBackedBufferSink.in")

  override val shape: SinkShape[T] = SinkShape.of(in)


  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, CompletableFuture[NotUsed]) = ???

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
