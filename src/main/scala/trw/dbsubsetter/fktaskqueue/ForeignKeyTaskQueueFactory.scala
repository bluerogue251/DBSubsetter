package trw.dbsubsetter.fktaskqueue

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.SchemaInfo

object ForeignKeyTaskQueueFactory {
  def build(config: Config, schemaInfo: SchemaInfo): ForeignKeyTaskQueue = {
    val base: ForeignKeyTaskQueue = new ForeignKeyTaskChronicleQueue(config, schemaInfo)
    new ForeignKeyTaskQueueInstrumented(base)
  }
}
