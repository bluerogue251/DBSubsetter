package trw.dbsubsetter.datacopy

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.SchemaInfo

object DataCopyQueueFactory {
  def buildDataCopyQueue(config: Config, schemaInfo: SchemaInfo): DataCopyQueue = {
    val base = new DataCopyQueueImpl(config, schemaInfo)
    new DataCopyQueueInstrumented(base)
  }
}
