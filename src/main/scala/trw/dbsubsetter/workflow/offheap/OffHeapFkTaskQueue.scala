package trw.dbsubsetter.workflow.offheap

import trw.dbsubsetter.workflow.ForeignKeyTask

trait OffHeapFkTaskQueue {
  def enqueue(fkOrdinal: Short, fkValue: Any, fetchChildren: Boolean): Unit
  def dequeue(): Option[ForeignKeyTask]
}
