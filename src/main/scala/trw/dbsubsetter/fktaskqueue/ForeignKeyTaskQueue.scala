package trw.dbsubsetter.fktaskqueue

import trw.dbsubsetter.workflow.ForeignKeyTask

trait ForeignKeyTaskQueue {
  def enqueue(foreignKeyTask: ForeignKeyTask): Unit
  def dequeue(): Option[ForeignKeyTask]
  def isEmpty(): Boolean
  def nonEmpty(): Boolean
  def size(): Long
}
