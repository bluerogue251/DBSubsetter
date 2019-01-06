package trw.dbsubsetter.workflow.offheap

import trw.dbsubsetter.workflow.{FkTask, NewTasks}

trait OffHeapFkTaskQueue {
  def enqueue(rawTasks: NewTasks): Unit
  def dequeue(): Option[FkTask]
}
