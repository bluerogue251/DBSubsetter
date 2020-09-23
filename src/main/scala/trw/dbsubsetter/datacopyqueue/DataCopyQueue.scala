package trw.dbsubsetter.datacopyqueue

import trw.dbsubsetter.workflow.{DataCopyTask, PksAdded}

trait DataCopyQueue {

  def enqueue(pksAdded: PksAdded): Unit

  /**
    * @return An optional DataCopyTask. The number of pkValues contained in this DataCopyTask is guaranteed to be
    *         contained in the list of standard batch sizes declared in Constants.dataCopyBatchSizes
    */
  def dequeue(): Option[DataCopyTask]

  def isEmpty(): Boolean
}
