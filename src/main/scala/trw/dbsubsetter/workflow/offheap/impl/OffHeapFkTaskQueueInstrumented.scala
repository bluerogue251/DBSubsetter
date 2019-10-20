package trw.dbsubsetter.workflow.offheap.impl

import trw.dbsubsetter.metrics.Metrics
import trw.dbsubsetter.workflow.offheap.OffHeapFkTaskQueue
import trw.dbsubsetter.workflow.{ForeignKeyTask, NewTasks}

private[offheap] final class OffHeapFkTaskQueueInstrumented(delegatee: OffHeapFkTaskQueue) extends OffHeapFkTaskQueue {

  private[this] val pendingTaskCount = Metrics.PendingTasksGauge

  private[this] val taskEnqueueDuration = Metrics.TaskEnqueueDuration

  override def enqueue(rawTasks: NewTasks): Unit = {
    val runnable: Runnable = () => delegatee.enqueue(rawTasks)
    val enqueueDuration: Double = taskEnqueueDuration.time(runnable)

    pendingTaskCount.inc(rawTasks.taskInfo.values.map(_.length).sum)
  }

  override def dequeue(): Option[ForeignKeyTask] = {
    delegatee.dequeue().map(fkTask => {
      pendingTaskCount.dec()
      fkTask
    })
  }
}
