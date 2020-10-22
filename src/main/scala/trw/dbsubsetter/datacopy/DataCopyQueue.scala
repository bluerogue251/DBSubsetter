package trw.dbsubsetter.datacopy

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.pkstore.PksAdded

trait DataCopyQueue {

  def enqueue(pksAdded: PksAdded): Unit

  /**
    * @return An optional DataCopyTask. The number of pkValues contained in this DataCopyTask is guaranteed to be
    *         contained in the list of standard batch sizes declared in Constants.dataCopyBatchSizes
    */
  def dequeue(): Option[DataCopyTask]

  def isEmpty(): Boolean
}

object DataCopyQueue {
  def from(config: Config, schemaInfo: SchemaInfo): DataCopyQueue = {
    val base = new DataCopyQueueImpl(config, schemaInfo)
    new DataCopyQueueInstrumentedImpl(base)
  }
}
