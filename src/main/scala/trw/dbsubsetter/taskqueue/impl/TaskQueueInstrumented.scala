package trw.dbsubsetter.taskqueue.impl

import trw.dbsubsetter.metrics.Metrics
import trw.dbsubsetter.taskqueue.TaskQueue
import trw.dbsubsetter.workflow.OriginDbRequest

private[taskqueue] class TaskQueueInstrumented(delegatee: TaskQueue) extends TaskQueue {

  private[this] val metrics = Metrics.PendingTasksGauge

  override def nonEmpty: Boolean = delegatee.nonEmpty

  override def enqueue(tasks: IndexedSeq[OriginDbRequest]): Unit = {
    metrics.inc(tasks.length)
    delegatee.enqueue(tasks)
  }

  override def dequeue(): OriginDbRequest = {
    metrics.dec()
    delegatee.dequeue()
  }
}
