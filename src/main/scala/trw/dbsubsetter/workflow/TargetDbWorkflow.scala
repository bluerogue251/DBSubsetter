package trw.dbsubsetter.workflow

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{SchemaInfo, TargetDbAccess}


class TargetDbWorkflow(config: Config, schemaInfo: SchemaInfo) {
  val db = new TargetDbAccess(config.originDbConnectionString, schemaInfo)

  def process(request: TargetDbInsertRequest): TargetDbInsertResult = {
    val rowsInserted = db.insertRows(request.table, request.rows)
    TargetDbInsertResult(request.table, rowsInserted)
  }
}
