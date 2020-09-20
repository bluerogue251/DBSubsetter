package trw.dbsubsetter.db

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.workflow.BaseQuery

object BaseQueries {
  def get(config: Config, sch: SchemaInfo): Seq[BaseQuery] = {
    config.baseQueries.map { baseQuery =>
      val selectColumns = sch.keyColumnsByTableOrdered(baseQuery.table)
      val sqlString = Sql.makeQueryString(baseQuery.table, selectColumns, baseQuery.whereClause, sch)
      BaseQuery(baseQuery.table, sqlString, baseQuery.includeChildren)
    }
  }
}
