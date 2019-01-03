package trw.dbsubsetter.singlethreaded.impl

import trw.dbsubsetter.singlethreaded.TaskTracker
import trw.dbsubsetter.workflow.OriginDbRequest

import scala.collection.mutable

/*
 * TODO -- maybe differentiate between "In-progress" tasks and "Pending" tasks
 * to make the count more accurate?
 */
private[singlethreaded] class TaskTrackerImpl extends TaskTracker {

  private[this] val queue = mutable.Queue.empty[OriginDbRequest]

  override def hasNextTask: Boolean = queue.nonEmpty

  override def enqueueNewTasks(tasks: IndexedSeq[OriginDbRequest]): Unit = queue.enqueue(tasks:_*)

  override def dequeueNextTask(): OriginDbRequest = queue.dequeue()
}
