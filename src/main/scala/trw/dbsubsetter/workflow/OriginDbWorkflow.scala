package trw.dbsubsetter.workflow

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{OriginDbAccess, SchemaInfo}


class OriginDbWorkflow(config: Config, schemaInfo: SchemaInfo) {
  val db = new OriginDbAccess(config.originDbConnectionString, schemaInfo)

  def process(request: OriginDbRequest): OriginDbResult = {
    request match {
      case FkTask(table, foreignKey, fkValue, fetchChildren) =>
        val rows = db.getRowsFromTemplate(foreignKey, table, fkValue)
        OriginDbResult(table, rows, fetchChildren)
      case SqlStrQuery(table, sql, fetchChildren) =>
        val rows = db.getRows(sql, table)
        OriginDbResult(table, rows, fetchChildren)
    }
  }
}
