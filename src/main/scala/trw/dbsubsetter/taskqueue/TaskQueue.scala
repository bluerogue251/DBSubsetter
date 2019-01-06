package trw.dbsubsetter.taskqueue

import trw.dbsubsetter.workflow.OriginDbRequest

trait TaskQueue {
  def nonEmpty: Boolean
  // `IndexedSeq` guarantees calls to `tasks.length()` will be O(1)
  def enqueue(tasks: IndexedSeq[OriginDbRequest]): Unit
  def dequeue(): OriginDbRequest
}