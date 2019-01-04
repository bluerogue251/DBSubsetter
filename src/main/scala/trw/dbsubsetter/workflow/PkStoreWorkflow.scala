package trw.dbsubsetter.workflow

import trw.dbsubsetter.db.Row
import trw.dbsubsetter.primarykeystore.PrimaryKeyStore


class PkStoreWorkflow(pkStore: PrimaryKeyStore) {

  // Calling this exists() seems not quite accurate
  // Also -- we should now be calling this solely from Akka-Streams
  // Consider converting into a filtering Flow and living directly in Akka-Streams?
  // And maybe that would allow us to remove the `PkResult` trait from `ForeignKeyTask`?
  // Thinking about it more -- if anything, this should be of type `def exists(task: FetchParentTask)`
  // since I believe at one point I was certain there should never be a reason to query this for a `FetchChildrenTask`
  // If the only reason we need this method to return the PkResult (as opposed to a boolean) was to get around
  // the non-Threadsafe nature of the PkStore itself, maybe we could decorate the PkStore with some locks
  // to make a Concurrent/threadsafe version we would use only from Akka-Streams, and then just use that directly for the exists() call?
  // Or, do we even need a threadsafe version of it? Isn't it enough to just have it be in one Flow, and we'll be guaranteed it'll only be called once at a time?
  def exists(task: ForeignKeyTask): PkResult = {
    val alreadyProcessed: Boolean = task match {
      case FetchParentTask(foreignKey, value) => pkStore.alreadySeen(foreignKey.toTable, value)
      case FetchChildrenTask(foreignKey, value) => pkStore.alreadySeenWithChildren(foreignKey.fromTable, value)
    }

    if (alreadyProcessed)
      DuplicateTask
    else
      task
  }

  def add(req: OriginDbResult): PksAdded = {
    val OriginDbResult(table, rows, viaTableOpt, fetchChildren) = req

    val pkOrdinals = sch.pksByTableOrdered(table).map(_.ordinalPosition)
    val pkOrdinal = pkOrdinals.head
    val isSingleColPk = pkOrdinals.lengthCompare(1) == 0
    val getPkValue: Row => Any = if (isSingleColPk) row => row(pkOrdinal) else row => pkOrdinals.map(row)

    if (fetchChildren) {
      // May need to make the return value of pkStore.markSEenWithChildren richer to represent if we had already fetched the parents
      // So that we don't do too much extra work here
      val parentsNotYetFetched = childrenNotYetFetched.filterNot(row => parentStore.remove(getPkValue(row)))
      val childrenNotYetFetched = rows.filter(row => pkStore.markSeenWithChildren(getPkValue(row)))
      PksAdded(table, parentsNotYetFetched, childrenNotYetFetched, viaTableOpt)
    } else {
      val newRows = rows.filter(row => pkStore.markSeen(table, getPkValue(row)))
      PksAdded(table, newRows, Vector.empty, viaTableOpt)
    }
  }
}