package trw.dbsubsetter.basequery

import trw.dbsubsetter.OriginDbResult
import trw.dbsubsetter.config.BaseQuery
import trw.dbsubsetter.db.{Keys, OriginDbAccess}
import trw.dbsubsetter.keyingestion.KeyIngester

final class BaseQueryPhaseImpl(
    baseQueries: Set[BaseQuery],
    dbAccess: OriginDbAccess,
    keyIngester: KeyIngester
) extends BaseQueryPhase {

  override def runPhase(): Unit = {
    baseQueries.foreach { baseQuery =>
      val keys: Vector[Keys] =
        dbAccess.getRowsFromWhereClause(
          baseQuery.table,
          baseQuery.whereClause
        )
      val dbResult: OriginDbResult = OriginDbResult(baseQuery.table, keys, None, baseQuery.includeChildren)
      keyIngester.ingest(dbResult)
    }
  }
}
