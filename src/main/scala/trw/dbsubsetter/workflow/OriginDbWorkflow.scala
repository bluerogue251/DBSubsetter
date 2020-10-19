package trw.dbsubsetter.workflow

import trw.dbsubsetter.db.{DbAccessFactory, Table}

final class OriginDbWorkflow(dbAccessFactory: DbAccessFactory) {

  private[this] val dbAccess = dbAccessFactory.buildOriginDbAccess()

  def process(request: ForeignKeyTask): OriginDbResult = {
    val result = request match {
      case FetchParentTask(foreignKey, fkValueFromChild) =>
        val table: Table = foreignKey.toTable
        val rows = dbAccess.getRowsFromForeignKeyValue(foreignKey, table, fkValueFromChild)
        OriginDbResult(table, rows, viaTableOpt = None, fetchChildren = false)
      case FetchChildrenTask(foreignKey, fkValueFromParent) =>
        val table: Table = foreignKey.fromTable
        val rows = dbAccess.getRowsFromForeignKeyValue(foreignKey, table, fkValueFromParent)
        OriginDbResult(table, rows, viaTableOpt = Some(foreignKey.toTable), fetchChildren = true)
    }
    result
  }
}
