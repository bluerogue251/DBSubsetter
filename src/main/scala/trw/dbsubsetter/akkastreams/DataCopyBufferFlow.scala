package trw.dbsubsetter.akkastreams

import akka.stream._
import akka.stream.stage._
import trw.dbsubsetter.datacopyqueue.DataCopyQueue
import trw.dbsubsetter.workflow._

// Adapted from https://github.com/torodb/akka-chronicle-queue
// TODO refactor to enable code sharing with FkTaskBufferFlow
// How does this flow know not to complete yet when upstream is complete but there are still remaining values in the buffer?
private[akkastreams] final class DataCopyBufferFlow(dataCopyQueue: DataCopyQueue) extends GraphStage[FlowShape[PksAdded, DataCopyTask]] {

  private[this] val in: Inlet[PksAdded] = Inlet.create[PksAdded]("DataCopyBufferFlow.in")

  private[this] val out: Outlet[DataCopyTask] = Outlet.create[DataCopyTask]("DataCopyBufferFlow.out")

  override val shape: FlowShape[PksAdded, DataCopyTask] = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

    setHandler(in, new InHandler {
      override def onPush(): Unit = {
        val pksAdded: PksAdded = grab(in)

        dataCopyQueue.enqueue(pksAdded)

        if (isAvailable(out)) {
          doPull()
        }

        pull(in)
      }
    })

    setHandler(out, new OutHandler {
      override def onPull(): Unit = doPull()
    })

    override def preStart(): Unit = {
      pull(in)
    }

    private[this] def doPull(): Unit = {
      val optionalTask: Option[DataCopyTask] = dataCopyQueue.dequeue()
      optionalTask.foreach(dataCopyTask => push[DataCopyTask](out, dataCopyTask))
    }
  }
}
