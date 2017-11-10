package trw.dbsubsetter.orchestration

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{SchemaInfo, Sql}

import scala.collection.immutable

object BaseQueryMaker {
  def makeBaseQueries(config: Config, sch: SchemaInfo): immutable.Iterable[SqlStrQuery] = {
    config.baseQueries.map { case ((schemaName, tableName), whereClause) =>
      val table = sch.tablesByName((schemaName, tableName))
      val (sqlString, cols) = Sql.makeQueryString(table, whereClause, sch, includeChildren = true)
      SqlStrQuery(table, cols, sqlString)
    }
  }
}
