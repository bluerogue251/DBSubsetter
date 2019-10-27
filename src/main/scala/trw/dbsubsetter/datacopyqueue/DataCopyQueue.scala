package trw.dbsubsetter.datacopyqueue

import trw.dbsubsetter.workflow.PksAdded


trait DataCopyQueue {
  def enqueue(pksAdded: PksAdded): Unit
  def dequeue(): Option[PksAdded]
}
