package trw.dbsubsetter.db

import trw.dbsubsetter.config.SchemaConfig
import trw.dbsubsetter.workflow.BaseQuery

object BaseQueries {
  def get(schemaConfig: SchemaConfig, schemaInfo: SchemaInfo): Set[BaseQuery] = {
    schemaConfig.baseQueries.map { baseQuery =>
      val selectColumns = schemaInfo.keyColumnsByTableOrdered(baseQuery.table)
      val sqlString = Sql.makeQueryString(baseQuery.table, selectColumns, baseQuery.whereClause)
      BaseQuery(baseQuery.table, sqlString, baseQuery.includeChildren)
    }
  }
}
