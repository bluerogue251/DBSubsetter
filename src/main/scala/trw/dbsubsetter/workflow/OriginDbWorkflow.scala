package trw.dbsubsetter.workflow

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.DbAccessFactory
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.db.Table

final class OriginDbWorkflow(config: Config, schemaInfo: SchemaInfo, dbAccessFactory: DbAccessFactory) {

  private[this] val dbAccess = dbAccessFactory.buildOriginDbAccess()

  def process(request: OriginDbRequest): OriginDbResult = {
    val result = request match {
      case FetchParentTask(foreignKey, fkValueFromChild) =>
        val table: Table = foreignKey.toTable
        val rows = dbAccess.getRowsFromForeignKeyValue(foreignKey, table, fkValueFromChild)
        OriginDbResult(table, rows, viaTableOpt = None, fetchChildren = false)
      case FetchChildrenTask(foreignKey, fkValueFromParent) =>
        val table: Table = foreignKey.fromTable
        val rows = dbAccess.getRowsFromForeignKeyValue(foreignKey, table, fkValueFromParent)
        OriginDbResult(table, rows, viaTableOpt = Some(foreignKey.toTable), fetchChildren = true)
      case BaseQuery(table, sql, fetchChildren) =>
        val rows = dbAccess.getRows(sql, table)
        OriginDbResult(table, rows, viaTableOpt = None, fetchChildren)
    }
    result
  }
}
