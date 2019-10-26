package trw.dbsubsetter.workflow

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{DbAccessFactory, SchemaInfo}


final class OriginDbWorkflow(config: Config, schemaInfo: SchemaInfo, dbAccessFactory: DbAccessFactory) {

  private[this] val dbAccess = dbAccessFactory.buildOriginDbAccess()

  def process(request: OriginDbRequest): OriginDbResult = {
    val result = request match {
      case FetchParentTask(parentTable, foreignKey, fkValueFromChild) =>
        val rows = dbAccess.getRowsFromForeignKeyValue(foreignKey, parentTable, fkValueFromChild)
        OriginDbResult(parentTable, rows, viaTableOpt = None, fetchChildren = false)
      case FetchChildrenTask(childTable, viaParentTable, foreignKey, fkValueFromParent) =>
        val rows = dbAccess.getRowsFromForeignKeyValue(foreignKey, childTable, fkValueFromParent)
        OriginDbResult(childTable, rows, viaTableOpt = Some(viaParentTable), fetchChildren = true)
      case BaseQuery(table, sql, fetchChildren) =>
        val rows = dbAccess.getRows(sql, table)
        OriginDbResult(table, rows, viaTableOpt = None, fetchChildren)
    }
    result
  }
}
