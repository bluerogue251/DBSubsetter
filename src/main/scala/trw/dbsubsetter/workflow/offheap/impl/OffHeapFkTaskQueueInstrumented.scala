package trw.dbsubsetter.workflow.offheap.impl

import trw.dbsubsetter.metrics.Metrics
import trw.dbsubsetter.workflow.offheap.OffHeapFkTaskQueue
import trw.dbsubsetter.workflow.{ForeignKeyTask, NewTasks}

private[offheap] final class OffHeapFkTaskQueueInstrumented(delegatee: OffHeapFkTaskQueue) extends OffHeapFkTaskQueue {

  private[this] val metrics = Metrics.PendingTasksGauge

  override def enqueue(rawTasks: NewTasks): Unit = {
    metrics.inc(rawTasks.taskInfo.values.map(_.length).sum)
    delegatee.enqueue(rawTasks)
  }

  override def dequeue(): Option[ForeignKeyTask] = {
    delegatee.dequeue().map(fkTask => {
      metrics.dec()
      fkTask
    })
  }
}
