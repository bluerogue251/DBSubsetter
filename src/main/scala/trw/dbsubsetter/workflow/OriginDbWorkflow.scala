package trw.dbsubsetter.workflow

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{DbAccessFactory, SchemaInfo}


class OriginDbWorkflow(config: Config, schemaInfo: SchemaInfo, dbAccessFactory: DbAccessFactory) {

  private[this] val dbAccess = dbAccessFactory.buildOriginDbAccess()

  def process(request: OriginDbRequest): OriginDbResult = {
    val result = request match {
      case FkTask(table, foreignKey, fkValue, fetchChildren) =>
        val rows = dbAccess.getRowsFromTemplate(foreignKey, table, fkValue)
        val viaTableOpt = if (fetchChildren) Some(foreignKey.toTable) else None
        OriginDbResult(table, rows, viaTableOpt, fetchChildren)
      case BaseQuery(table, sql, fetchChildren) =>
        val rows = dbAccess.getRows(sql, table)
        OriginDbResult(table, rows, viaTableOpt = None, fetchChildren)
    }
    result
  }
}