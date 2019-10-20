package trw.dbsubsetter.workflow.offheap.impl

import io.prometheus.client.Histogram.Timer
import trw.dbsubsetter.metrics.Metrics
import trw.dbsubsetter.workflow.ForeignKeyTask
import trw.dbsubsetter.workflow.offheap.OffHeapFkTaskQueue

private[offheap] final class OffHeapFkTaskQueueInstrumented(delegatee: OffHeapFkTaskQueue) extends OffHeapFkTaskQueue {

  private[this] val pendingTaskCount = Metrics.PendingTasksGauge

  private[this] val taskEnqueueDuration = Metrics.TaskEnqueueDuration

  private[this] val taskDequeueDuration = Metrics.TaskDequeueDuration

  override def enqueue(fkOrdinal: Short, fkValue: Any, fetchChildren: Boolean): Unit = {
    val runnable: Runnable = () => delegatee.enqueue(fkOrdinal, fkValue, fetchChildren)
    taskEnqueueDuration.time(runnable)
    pendingTaskCount.inc()
  }

  override def dequeue(): Option[ForeignKeyTask] = {
    val timer: Timer = taskDequeueDuration.startTimer()
    val optionalTask: Option[ForeignKeyTask] = delegatee.dequeue()
    optionalTask.foreach(_ => {
      pendingTaskCount.dec()
      timer.observeDuration()
    })
    optionalTask
  }
}
