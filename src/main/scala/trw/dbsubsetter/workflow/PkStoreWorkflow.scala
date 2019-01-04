package trw.dbsubsetter.workflow

import trw.dbsubsetter.db.Row
import trw.dbsubsetter.primarykeystore.PrimaryKeyStore


class PkStoreWorkflow(pkStore: PrimaryKeyStore) {

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
    val (parentStore, childStore) = getStorage(table)

    val pkOrdinals = sch.pksByTableOrdered(table).map(_.ordinalPosition)
    val pkOrdinal = pkOrdinals.head
    val isSingleColPk = pkOrdinals.lengthCompare(1) == 0
    val getPkValue: Row => Any = if (isSingleColPk) row => row(pkOrdinal) else row => pkOrdinals.map(row)

    if (fetchChildren) {
      val childrenNotYetFetched = rows.filter(row => childStore.add(getPkValue(row)))
      val parentsNotYetFetched = childrenNotYetFetched.filterNot(row => parentStore.remove(getPkValue(row)))
      PksAdded(table, parentsNotYetFetched, childrenNotYetFetched, viaTableOpt)
    } else {
      val newRows = rows.filter { row =>
        val pkValue = getPkValue(row)
        !childStore.contains(pkValue) && parentStore.add(pkValue)
      }
      PksAdded(table, newRows, Vector.empty, viaTableOpt)
    }
  }
}