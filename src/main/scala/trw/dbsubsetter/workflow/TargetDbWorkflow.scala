package trw.dbsubsetter.workflow

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{ConnectionFactory, SchemaInfo, TargetDbAccess}


class TargetDbWorkflow(config: Config, schemaInfo: SchemaInfo, connectionFactory: ConnectionFactory) {
  private[this] val db = new TargetDbAccess(config.targetDbConnectionString, schemaInfo, connectionFactory)

  // The fact that a row still needs parent tasks means this is the first time we've seen it
  // I.e. it has not yet been added to the target db
  def process(request: PksAdded): TargetDbInsertResult = {
    val rowsInserted = db.insertRows(request.table, request.rowsNeedingParentTasks)
    TargetDbInsertResult(request.table, rowsInserted)
  }
}
