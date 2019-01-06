package trw.dbsubsetter.workflow

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{DbAccessFactory, SchemaInfo}


class OriginDbWorkflow(config: Config, schemaInfo: SchemaInfo, dbAccessFactory: DbAccessFactory) {

  private[this] val dbAccess = dbAccessFactory.buildOriginDbAccess()

  def process(request: OriginDbRequest): OriginDbResult = {
    val result = request match {
      case FetchParentTask(table, foreignKey, fkValue) =>
        val rows = dbAccess.getRowsFromTemplate(foreignKey, table, fkValue)
        OriginDbResult(table, rows, viaTableOpt = None, fetchChildren = false)
      case FetchChildrenTask(table, foreignKey, fkValue) =>
        val rows = dbAccess.getRowsFromTemplate(foreignKey, table, fkValue)
        OriginDbResult(table, rows, viaTableOpt = Some(foreignKey.toTable), fetchChildren = false)
      case BaseQuery(table, sql, fetchChildren) =>
        val rows = dbAccess.getRows(sql, table)
        OriginDbResult(table, rows, viaTableOpt = None, fetchChildren)
    }
    result
  }
}