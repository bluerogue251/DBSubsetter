package trw.dbsubsetter.workflow

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{OriginDbAccess, SchemaInfo}
import trw.dbsubsetter.util.Closeable


class OriginDbWorkflow(config: Config, schemaInfo: SchemaInfo) extends Closeable {
  private val db = new OriginDbAccess(config.originDbConnectionString, schemaInfo)

  def process(request: OriginDbRequest): OriginDbResult = {
    request match {
      case FkTask(table, foreignKey, fkValue, fetchChildren) =>
        val rows = db.getRowsFromTemplate(foreignKey, table, fkValue)
        val viaTableOpt = if (fetchChildren) Some(foreignKey.toTable) else None
        OriginDbResult(table, rows, viaTableOpt, fetchChildren)
      case SqlStrQuery(table, sql, fetchChildren) =>
        val rows = db.getRows(sql, table)
        OriginDbResult(table, rows, None, fetchChildren)
    }
  }

  override def close(): Unit = db.close()
}
