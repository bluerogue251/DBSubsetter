package trw.dbsubsetter.taskqueue.impl

import trw.dbsubsetter.taskqueue.TaskQueue
import trw.dbsubsetter.workflow.OriginDbRequest

import scala.collection.mutable

private[taskqueue] class TaskQueueImpl extends TaskQueue {

  private[this] val queue = mutable.Queue.empty[OriginDbRequest]

  override def nonEmpty: Boolean = queue.nonEmpty

  override def enqueueTasks(tasks: IndexedSeq[OriginDbRequest]): Unit = queue.enqueue(tasks:_*)

  override def dequeueTask(): OriginDbRequest = queue.dequeue()
}
