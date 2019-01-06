package trw.dbsubsetter.tasktracker.impl

import trw.dbsubsetter.metrics.Metrics
import trw.dbsubsetter.tasktracker.TaskTracker
import trw.dbsubsetter.workflow.OriginDbRequest

private[tasktracker] class TaskTrackerInstrumented(delegatee: TaskTracker) extends TaskTracker {

  private[this] val metrics = Metrics.PendingTasksGauge

  override def nonEmpty: Boolean = delegatee.nonEmpty

  override def enqueueTasks(tasks: IndexedSeq[OriginDbRequest]): Unit = {
    metrics.inc(tasks.length)
    delegatee.enqueueTasks(tasks)
  }

  override def dequeueTask(): OriginDbRequest = {
    metrics.dec()
    delegatee.dequeueTask()
  }
}
