package trw.dbsubsetter.taskqueue.impl

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.taskqueue.TaskQueue
import trw.dbsubsetter.workflow.offheap.{OffHeapFkTaskQueue, OffHeapFkTaskQueueFactory}
import trw.dbsubsetter.workflow.{BaseQuery, OriginDbRequest}

import scala.collection.mutable

private[taskqueue] final class TaskQueueImpl(config: Config, schemaInfo: SchemaInfo) extends TaskQueue {

  private[this] val baseQueryQueue: mutable.Queue[BaseQuery] =
    mutable.Queue.empty[BaseQuery]

  private[this] val fkTaskQueue: OffHeapFkTaskQueue =
    OffHeapFkTaskQueueFactory.buildOffHeapFkTaskQueue(config, schemaInfo)

  private[this] var dequeueFunction: () => OriginDbRequest =
    () => {
      baseQueryQueue.dequeue()
      if (baseQueryQueue.isEmpty) dequeueFunction = () => fkTaskQueue.dequeue()
    }

  override def nonEmpty: Boolean = baseQueryQueue.nonEmpty

  override def enqueue(tasks: IndexedSeq[OriginDbRequest]): Unit = baseQueryQueue.enqueue(tasks: _*)

  override def dequeue(): OriginDbRequest = dequeueFunction.apply()
}
