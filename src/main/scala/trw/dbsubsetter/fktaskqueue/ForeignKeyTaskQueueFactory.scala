package trw.dbsubsetter.fktaskqueue

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.fktaskqueue.impl.ForeignKeyTaskChronicleQueue
import trw.dbsubsetter.fktaskqueue.impl.ForeignKeyTaskQueueInstrumented

object ForeignKeyTaskQueueFactory {
  def build(config: Config, schemaInfo: SchemaInfo): ForeignKeyTaskQueue = {
    var fkTaskQueue: ForeignKeyTaskQueue =
      new ForeignKeyTaskChronicleQueue(config, schemaInfo)

    if (config.exposeMetrics) {
      fkTaskQueue = new ForeignKeyTaskQueueInstrumented(fkTaskQueue)
    }

    fkTaskQueue
  }
}
