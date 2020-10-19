package trw.dbsubsetter.basequery

import trw.dbsubsetter.config.SchemaConfig
import trw.dbsubsetter.db.SchemaInfo

object BaseQueries {
  def get(schemaConfig: SchemaConfig, schemaInfo: SchemaInfo): Set[BaseQuery] = {
    schemaConfig.baseQueries.map { baseQuery =>
      BaseQuery(baseQuery.table, sqlString, baseQuery.includeChildren)
    }
  }
}
