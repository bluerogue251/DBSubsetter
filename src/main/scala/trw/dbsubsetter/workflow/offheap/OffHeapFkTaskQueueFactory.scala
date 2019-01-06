package trw.dbsubsetter.workflow.offheap

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.workflow.offheap.impl.FkTaskChronicleQueue

object OffHeapFkTaskQueueFactory {
  def buildOffHeapFkTaskQueue(config: Config, schemaInfo: SchemaInfo): OffHeapFkTaskQueue = {
    new FkTaskChronicleQueue(config, schemaInfo)
  }
}
