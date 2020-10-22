package trw.dbsubsetter.keyingestion

import trw.dbsubsetter.OriginDbResult
import trw.dbsubsetter.datacopy.DataCopyQueue
import trw.dbsubsetter.fkcalc.{FkTaskGenerator, ForeignKeyTask}
import trw.dbsubsetter.fktaskqueue.ForeignKeyTaskQueue
import trw.dbsubsetter.pkstore.{PkStoreWorkflow, PksAdded}

final class KeyIngesterImpl(
    pkWorkflow: PkStoreWorkflow,
    dataCopyQueue: DataCopyQueue,
    fkTaskGenerator: FkTaskGenerator,
    fkTaskQueue: ForeignKeyTaskQueue
) extends KeyIngester {
  override def ingest(originDbResult: OriginDbResult): Long = {
    val pksAdded: PksAdded = pkWorkflow.add(originDbResult)

    // Queue up the newly seen rows to be copied into the target database
    dataCopyQueue.enqueue(pksAdded)

    // Queue up any new tasks resulting from this stage
    val newFkTasks: IndexedSeq[ForeignKeyTask] = fkTaskGenerator.generateFrom(pksAdded)
    newFkTasks.foreach(fkTaskQueue.enqueue)
    newFkTasks.size
  }
}
