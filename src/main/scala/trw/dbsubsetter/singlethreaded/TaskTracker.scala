package trw.dbsubsetter.singlethreaded

import trw.dbsubsetter.workflow.OriginDbRequest

trait TaskTracker {
  def hasNextTask: Boolean

  // `IndexedSeq` guarantees calls to `tasks.length()` will be O(1)
  def enqueueNewTasks(tasks: IndexedSeq[OriginDbRequest]): Unit

  def dequeueNextTask(): OriginDbRequest
}
