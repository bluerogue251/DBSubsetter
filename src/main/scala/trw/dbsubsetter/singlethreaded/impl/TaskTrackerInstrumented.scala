package trw.dbsubsetter.singlethreaded.impl

import trw.dbsubsetter.metrics.Metrics
import trw.dbsubsetter.singlethreaded.TaskTracker
import trw.dbsubsetter.workflow.OriginDbRequest

private[singlethreaded] class TaskTrackerInstrumented(delegatee: TaskTracker) extends TaskTracker {

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
