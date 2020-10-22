package trw.dbsubsetter.datacopyqueue.impl

import trw.dbsubsetter.datacopy.DataCopyTask
import trw.dbsubsetter.datacopyqueue.DataCopyQueue
import trw.dbsubsetter.metrics.Metrics
import trw.dbsubsetter.pkstore.PksAdded

private[datacopyqueue] final class DataCopyQueueInstrumented(delegatee: DataCopyQueue) extends DataCopyQueue {

  private[this] val pendingTaskCount = Metrics.PendingDataCopyRows

  private[this] val taskEnqueueDuration = Metrics.DataCopyTaskEnqueueDuration

  private[this] val taskDequeueDuration = Metrics.DataCopyTaskDequeueDuration

  override def enqueue(pksAdded: PksAdded): Unit = {
    val runnable: Runnable = () => delegatee.enqueue(pksAdded)
    taskEnqueueDuration.time(runnable)
    pendingTaskCount.inc(pksAdded.rowsNeedingParentTasks.length)
  }

  override def dequeue(): Option[DataCopyTask] = {
    val optionalResult: Option[DataCopyTask] = taskDequeueDuration.time(() => delegatee.dequeue())
    optionalResult.foreach(dataCopyTask => pendingTaskCount.dec(dataCopyTask.pkValues.size))
    optionalResult
  }

  override def isEmpty(): Boolean = {
    delegatee.isEmpty()
  }
}
