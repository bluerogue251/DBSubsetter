package trw.dbsubsetter.workflow

import trw.dbsubsetter.db.{Row, Table}

import scala.collection.mutable


class PkStoreWorkflow(pkOrdinalsByTable: Map[Table, Seq[Int]]) {
  private val tables = pkOrdinalsByTable.keys
  // Left side of the tuple is for parents, right side of the tuple is for children
  // If a PK is on the children side, then both its children AND its parents have been fetched.
  // IF a PK is on the parent side, then only its parents have been fetched
  // There is no such thing as having fetched a row's children but not having fetched its parents
  // If a PK is in there at all, then at any given time, it is either in the parent set or child set, never both at once.
  //
  // Optimization may be possible by changing AnyRef to Any, and/or changing HashSet to TreeSet
  // Using `Any` would in theory allow us to use primitive Ints instead of Integer for PK storage, etc.
  // TreeSet in theory might provide more stable runtime of operations, so that we never encounter one big
  // time when we need to double the size of the HashSet, etc.
  // Both potential optimizations would need testing: it's not clear how much if any good they would do
  private val pkStore: Map[Table, (mutable.HashSet[AnyRef], mutable.HashSet[AnyRef])] = tables.map { t =>
    t -> (mutable.HashSet.empty[AnyRef], mutable.HashSet.empty[AnyRef])
  }.toMap

  def exists(req: FkTask): PkResult = {
    val FkTask(table, _, fkValue, fetchChildren) = req
    val (parentStore, childStore) = getStorage(table)

    if (fetchChildren) {
      if (childStore.contains(fkValue)) DuplicateTask else req
    } else {
      if (parentStore.contains(fkValue) || childStore.contains(fkValue)) DuplicateTask else req
    }
  }

  def add(req: OriginDbResult): PksAdded = {
    val OriginDbResult(table, rows, viaTableOpt, fetchChildren) = req
    val (parentStore, childStore) = getStorage(table)

    val pkOrdinals = pkOrdinalsByTable(table)
    val pkOrdinal = pkOrdinals.head
    val isSingleColPk = pkOrdinals.size == 1
    val getPkValue: Row => AnyRef = if (isSingleColPk) row => row(pkOrdinal) else row => pkOrdinals.map(row)

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

  def getStorage(t: Table): (mutable.HashSet[AnyRef], mutable.HashSet[AnyRef]) = {
    pkStore.getOrElse(t, throw new RuntimeException(s"No primary key defined for table $t"))
  }
}