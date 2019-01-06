package trw.dbsubsetter.workflow

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{DbAccessFactory, SchemaInfo}


final class TargetDbWorkflow(config: Config, schemaInfo: SchemaInfo, dbAccessFactory: DbAccessFactory) {
  private[this] val dbAccess = dbAccessFactory.buildTargetDbAccess()

  // The fact that a row still needs parent tasks means this is the first time we've seen it
  // I.e. it has not yet been added to the target db
  def process(request: PksAdded): TargetDbInsertResult = {
    val rowsInserted = dbAccess.insertRows(request.table, request.rowsNeedingParentTasks)
    TargetDbInsertResult(request.table, rowsInserted)
  }
}
