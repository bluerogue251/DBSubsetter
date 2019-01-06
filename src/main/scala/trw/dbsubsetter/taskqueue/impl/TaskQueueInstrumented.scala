package trw.dbsubsetter.taskqueue.impl

import trw.dbsubsetter.metrics.Metrics
import trw.dbsubsetter.taskqueue.TaskQueue
import trw.dbsubsetter.workflow.OriginDbRequest

private[taskqueue] class TaskQueueInstrumented(delegatee: TaskQueue) extends TaskQueue {

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
