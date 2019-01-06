package trw.dbsubsetter.workflow.offheap

import trw.dbsubsetter.workflow.{ForeignKeyTask, NewTasks}

trait OffHeapFkTaskQueue {
  def enqueue(rawTasks: NewTasks): Unit
  def dequeue(): Option[ForeignKeyTask]
}
