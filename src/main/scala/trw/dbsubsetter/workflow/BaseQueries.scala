package trw.dbsubsetter.workflow

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{SchemaInfo, Sql}


object BaseQueries {
  def get(config: Config, sch: SchemaInfo): Iterable[SqlStrQuery] = {
    config.baseQueries.map { case ((schemaName, tableName), whereClause) =>
      val table = sch.tablesByName((schemaName, tableName))
      val sqlString = Sql.makeQueryString(table, whereClause, sch)
      SqlStrQuery(table, sqlString)
    }
  }
}
