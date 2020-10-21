package trw.dbsubsetter.keyingestion

import trw.dbsubsetter.datacopyqueue.DataCopyQueue
import trw.dbsubsetter.fktaskqueue.ForeignKeyTaskQueue
import trw.dbsubsetter.workflow._

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
