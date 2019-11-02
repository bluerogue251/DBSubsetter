package trw.dbsubsetter.db

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.workflow.BaseQuery

object BaseQueries {
  def get(config: Config, sch: SchemaInfo): Vector[BaseQuery] = {
    config.baseQueries.map { case ((schemaName, tableName), whereClause, fetchChildren) =>
      val table = sch.tablesByName((schemaName, tableName))
      val selectColumns = sch.keyColumnsByTableOrdered(table)
      val sqlString = Sql.makeQueryString(table, selectColumns, whereClause, sch)
      BaseQuery(table, sqlString, fetchChildren)
    }
  }
}
