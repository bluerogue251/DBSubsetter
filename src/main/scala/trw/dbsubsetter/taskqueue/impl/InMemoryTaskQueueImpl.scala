package trw.dbsubsetter.taskqueue.impl

import trw.dbsubsetter.taskqueue.TaskQueue
import trw.dbsubsetter.workflow.OriginDbRequest

import scala.collection.mutable

private[taskqueue] final class InMemoryTaskQueueImpl extends TaskQueue {

  private[this] val queue = mutable.Queue.empty[OriginDbRequest]

  override def nonEmpty: Boolean = queue.nonEmpty

  override def enqueue(tasks: Seq[OriginDbRequest]): Unit = queue.enqueue(tasks: _*)

  override def dequeue(): OriginDbRequest = queue.dequeue()
}
