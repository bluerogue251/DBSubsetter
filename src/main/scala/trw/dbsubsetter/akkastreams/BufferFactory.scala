package trw.dbsubsetter.akkastreams

import akka.stream.FlowShape
import akka.stream.stage.GraphStage
import trw.dbsubsetter.datacopyqueue.DataCopyQueue
import trw.dbsubsetter.db.{ForeignKey, ForeignKeyValue}
import trw.dbsubsetter.workflow.offheap.OffHeapFkTaskQueue
import trw.dbsubsetter.workflow.{DataCopyTask, ForeignKeyTask, NewTasks, PksAdded}

object BufferFactory {

  def dataCopyBuffer(dataCopyQueue: DataCopyQueue): GraphStage[FlowShape[PksAdded, DataCopyTask]] = {
    val backingQueue: TransformingQueue[PksAdded, DataCopyTask] =
      TransformingQueue.from[PksAdded, DataCopyTask](
        dataCopyQueue.enqueue,
        dataCopyQueue.dequeue _,
        dataCopyQueue.isEmpty _
      )

    new TransformingQueueBackedBufferFlow[PksAdded, DataCopyTask](backingQueue)
  }

  def fkTaskBuffer(fkTaskQueue: OffHeapFkTaskQueue): GraphStage[FlowShape[NewTasks, ForeignKeyTask]] = {

    val backingQueue: TransformingQueue[NewTasks, ForeignKeyTask] =
      new TransformingQueue[NewTasks, ForeignKeyTask] {

        private[this] var elementCount: Long = 0L

        override def enqueue(element: NewTasks): Unit = {
          val newTaskMap: Map[(ForeignKey, Boolean), Seq[ForeignKeyValue]] = element.taskInfo
          newTaskMap.foreach { case ((fk, fetchChildren), fkValues) =>
            fkValues.foreach { fkValue =>
              elementCount += 1
              fkTaskQueue.enqueue(fk.i, fkValue, fetchChildren)
            }
          }
        }

        override def dequeue(): Option[ForeignKeyTask] = {
          val optionalElement: Option[ForeignKeyTask]  = fkTaskQueue.dequeue()
          optionalElement.foreach(_ => elementCount -= 1)
          optionalElement
        }

        override def isEmpty(): Boolean = {
          elementCount == 0L
        }
      }

    new TransformingQueueBackedBufferFlow[NewTasks, ForeignKeyTask](backingQueue)
  }
}
