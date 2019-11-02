package trw.dbsubsetter.workflow.offheap

import trw.dbsubsetter.db.ForeignKeyValue
import trw.dbsubsetter.workflow.ForeignKeyTask

trait OffHeapFkTaskQueue {
  def enqueue(fkOrdinal: Short, fkValue: ForeignKeyValue, fetchChildren: Boolean): Unit
  def dequeue(): Option[ForeignKeyTask]
}
