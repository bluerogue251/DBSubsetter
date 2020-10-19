package trw.dbsubsetter.basequery

import trw.dbsubsetter.config.BaseQuery
import trw.dbsubsetter.datacopyqueue.DataCopyQueue
import trw.dbsubsetter.db.{Keys, OriginDbAccess}
import trw.dbsubsetter.fktaskqueue.ForeignKeyTaskQueue
import trw.dbsubsetter.workflow.{FkTaskGenerator, OriginDbResult, PkStoreWorkflow, PksAdded}

final class BaseQueryPhaseImpl(
    baseQueries: Seq[BaseQuery],
    dbAccess: OriginDbAccess,
    pkWorkflow: PkStoreWorkflow,
    dataCopyQueue: DataCopyQueue,
    fkTaskGenerator: FkTaskGenerator,
    fkTaskQueue: ForeignKeyTaskQueue
) extends BaseQueryPhase {

  override def runPhase(): Unit = {
    baseQueries.foreach { baseQuery =>
      // Fetch the primary and foreign key values from the origin database
      val keys: Vector[Keys] =
        dbAccess.getRowsFromWhereClause(
          baseQuery.table,
          baseQuery.whereClause
        )

      // Calculate which primary key values we've seen already
      val dbResult: OriginDbResult = OriginDbResult(baseQuery.table, keys, None, baseQuery.includeChildren)
      val pksAdded: PksAdded = pkWorkflow.add(dbResult)

      // Queue up the newly seen rows to be copied into the target database
      dataCopyQueue.enqueue(pksAdded)

      // Queue up any new tasks resulting from this stage
      fkTaskGenerator
        .generateFrom(pksAdded)
        .foreach(fkTaskQueue.enqueue)
    }
  }
}
