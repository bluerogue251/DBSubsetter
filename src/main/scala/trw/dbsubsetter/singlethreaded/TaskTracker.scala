package trw.dbsubsetter.singlethreaded

import trw.dbsubsetter.workflow.OriginDbRequest

trait TaskTracker {
  def hasNextTask: Boolean
  def enqueueNewTasks(tasks: IndexedSeq[OriginDbRequest]): Unit
  def dequeueNextTask(): OriginDbRequest
}
