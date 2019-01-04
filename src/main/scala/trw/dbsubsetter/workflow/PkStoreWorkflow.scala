package trw.dbsubsetter.workflow

import trw.dbsubsetter.db.Row
import trw.dbsubsetter.primarykeystore.PrimaryKeyStore


class PkStoreWorkflow(pkStore: PrimaryKeyStore) {

  // Calling this exists() seems not quite accurate
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