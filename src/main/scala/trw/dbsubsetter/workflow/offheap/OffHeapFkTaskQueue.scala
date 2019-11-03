package trw.dbsubsetter.workflow.offheap

import trw.dbsubsetter.workflow.ForeignKeyTask


trait OffHeapFkTaskQueue {
  def enqueue(foreignKeyTask: ForeignKeyTask): Unit
  def dequeue(): Option[ForeignKeyTask]
  def isEmpty(): Boolean
}
