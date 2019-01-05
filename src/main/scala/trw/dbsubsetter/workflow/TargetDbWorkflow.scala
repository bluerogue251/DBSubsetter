package trw.dbsubsetter.workflow

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{DbAccessFactory, Row, SchemaInfo}


class TargetDbWorkflow(config: Config, schemaInfo: SchemaInfo, dbAccessFactory: DbAccessFactory) {

  private[this] val dbAccess = dbAccessFactory.buildTargetDbAccess()

  def process(request: PksAdded): TargetDbInsertResult = {
    // The fact that a row still needs parent tasks means this is the first time we've seen it and
    // it therefore should be be added to the target db. Use IndexedSeq type signature to ensure O(1) .length call
    val rowsToInsert: IndexedSeq[Row] = request.rowsNeedingParentTasks
    val rowsInserted = dbAccess.insertRows(request.table, request.rowsNeedingParentTasks)
    if (rowsInserted != rowsToInsert.length) {
      val message = s"Unexpected number of rows inserted into target DB. Table: ${request.table}. Expected ${rowsToInsert.length}. Actual $rowsInserted."
      throw new RuntimeException(message)
    }
    TargetDbInsertResult(request.table, rowsInserted)
  }
}
