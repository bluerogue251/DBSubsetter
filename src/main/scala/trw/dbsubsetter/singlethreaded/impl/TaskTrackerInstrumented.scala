package trw.dbsubsetter.singlethreaded.impl

import trw.dbsubsetter.metrics.Metrics
import trw.dbsubsetter.singlethreaded.TaskTracker
import trw.dbsubsetter.workflow.OriginDbRequest

private[singlethreaded] class TaskTrackerInstrumented(delegatee: TaskTracker) extends TaskTracker {

  private[this] val metrics = Metrics.OutstandingTasksGauge

  override def hasNextTask: Boolean = delegatee.hasNextTask

  override def enqueueNewTasks(tasks: IndexedSeq[OriginDbRequest]): Unit = {
    metrics.inc(tasks.length)
    delegatee.enqueueNewTasks(tasks)
  }

  override def dequeueNextTask(): OriginDbRequest = {
    metrics.dec()
    delegatee.dequeueNextTask()
  }
}
