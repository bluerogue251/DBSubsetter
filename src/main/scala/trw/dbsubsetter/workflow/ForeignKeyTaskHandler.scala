package trw.dbsubsetter.workflow

import trw.dbsubsetter.db.{DbAccessFactory, Table}

final class ForeignKeyTaskHandler(dbAccessFactory: DbAccessFactory) {

  private[this] val dbAccess = dbAccessFactory.buildOriginDbAccess()

  def handle(task: ForeignKeyTask): OriginDbResult = {
    val result = task match {
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
