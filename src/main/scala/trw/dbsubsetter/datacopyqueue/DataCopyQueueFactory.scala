package trw.dbsubsetter.datacopyqueue

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.datacopyqueue.impl.DataCopyQueueInstrumented
import trw.dbsubsetter.db.SchemaInfo


object DataCopyQueueFactory {
  def buildDataCopyQueue(config: Config, schemaInfo: SchemaInfo): DataCopyQueue = {
    var dataCopyQueue: DataCopyQueue =
      ???

    if (config.exposeMetrics) {
      dataCopyQueue = new DataCopyQueueInstrumented(dataCopyQueue)
    }

    dataCopyQueue
  }
}
