package trw.dbsubsetter.fktaskqueue.impl

import trw.dbsubsetter.fktaskqueue.ForeignKeyTaskQueue
import trw.dbsubsetter.metrics.Metrics
import trw.dbsubsetter.workflow.ForeignKeyTask


private[fktaskqueue] final class ForeignKeyTaskQueueInstrumented(delegatee: ForeignKeyTaskQueue) extends ForeignKeyTaskQueue {

  private[this] val pendingTaskCount = Metrics.PendingForeignKeyTasks

  private[this] val taskEnqueueDuration = Metrics.ForeignKeyTaskEnqueueDuration

  private[this] val taskDequeueDuration = Metrics.ForeignKeyTaskDequeueDuration

  override def enqueue(foreignKeyTask: ForeignKeyTask): Unit = {
    val runnable: Runnable = () => delegatee.enqueue(foreignKeyTask)
    taskEnqueueDuration.time(runnable)
    pendingTaskCount.inc()
  }

  override def dequeue(): Option[ForeignKeyTask] = {
    val optionalTask: Option[ForeignKeyTask] = taskDequeueDuration.time(() => delegatee.dequeue())
    optionalTask.foreach(_ => pendingTaskCount.dec())
    optionalTask
  }

  override def isEmpty(): Boolean = {
    delegatee.isEmpty()
  }
}
