package trw.dbsubsetter.datacopyqueue

import trw.dbsubsetter.workflow.{DataCopyTask, PksAdded}


trait DataCopyQueue {
  def enqueue(pksAdded: PksAdded): Unit
  def dequeue(): Option[DataCopyTask]
}
