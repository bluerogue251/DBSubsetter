package trw.dbsubsetter.basequery

import trw.dbsubsetter.config.BaseQuery
import trw.dbsubsetter.db.OriginDbAccess
import trw.dbsubsetter.workflow.{OriginDbResult, PksAdded}

final class BaseQueryPhaseImpl(baseQueries: Seq[BaseQuery], dbAccess: OriginDbAccess) extends BaseQueryPhase {
  override def runPhase(): Unit = {
    baseQueries.foreach { baseQuery =>
      // Query the origin database
      val dbResult: OriginDbResult = dbAccess.getRowsFromWhereClause(baseQuery.)

      // Calculate which rows we've seen already
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
