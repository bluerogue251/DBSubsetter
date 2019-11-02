package trw.dbsubsetter.taskqueue

import trw.dbsubsetter.workflow.OriginDbRequest

trait TaskQueue {
  def nonEmpty: Boolean
  def enqueue(tasks: Seq[OriginDbRequest]): Unit
  def dequeue(): OriginDbRequest
}
