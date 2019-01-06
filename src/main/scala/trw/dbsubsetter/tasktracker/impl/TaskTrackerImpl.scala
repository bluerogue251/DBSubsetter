package trw.dbsubsetter.tasktracker.impl

import trw.dbsubsetter.tasktracker.TaskTracker
import trw.dbsubsetter.workflow.OriginDbRequest

import scala.collection.mutable

private[tasktracker] class TaskTrackerImpl extends TaskTracker {

  private[this] val queue = mutable.Queue.empty[OriginDbRequest]

  override def nonEmpty: Boolean = queue.nonEmpty

  override def enqueueTasks(tasks: IndexedSeq[OriginDbRequest]): Unit = queue.enqueue(tasks:_*)

  override def dequeueTask(): OriginDbRequest = queue.dequeue()
}
