package trw.dbsubsetter.akkastreams

import akka.stream.FlowShape
import akka.stream.stage.GraphStage
import trw.dbsubsetter.datacopyqueue.DataCopyQueue
import trw.dbsubsetter.workflow.offheap.OffHeapFkTaskQueue
import trw.dbsubsetter.workflow.{DataCopyTask, ForeignKeyTask, PksAdded}

object BufferFactory {

  def dataCopyBuffer(dataCopyQueue: DataCopyQueue): GraphStage[FlowShape[PksAdded, DataCopyTask]] = {
    val backingQueue: TransformingQueue[PksAdded, DataCopyTask] =
      TransformingQueue.from[PksAdded, DataCopyTask](
        dataCopyQueue.enqueue,
        dataCopyQueue.dequeue _,
        dataCopyQueue.isEmpty _
      )

    new QueueBackedBufferFlow[PksAdded, DataCopyTask](backingQueue)
  }

  def fkTaskBuffer(fkTaskQueue: OffHeapFkTaskQueue): GraphStage[FlowShape[ForeignKeyTask, ForeignKeyTask]] = {
    val backingQueue: TransformingQueue[ForeignKeyTask, ForeignKeyTask] =
      TransformingQueue.from[ForeignKeyTask, ForeignKeyTask](
        fkTaskQueue.enqueue,
        fkTaskQueue.dequeue _,
        fkTaskQueue.isEmpty _
      )

    new QueueBackedBufferFlow[ForeignKeyTask, ForeignKeyTask](backingQueue)
  }
}
