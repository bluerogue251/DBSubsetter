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
  private val pkStore: Map[Table, (mutable.HashSet[AnyRef], mutable.HashSet[AnyRef])] = tables.map { t =>
    t -> (mutable.HashSet.empty[AnyRef], mutable.HashSet.empty[AnyRef])
  }.toMap

  def process(request: PkRequest): PkResult = {
    val (parentStore, childStore) = pkStore.getOrElse(request.table, throw new RuntimeException(s"No primary key defined for table ${request.table.fullyQualifiedName}"))
    request match {
      case fkt@FkTask(_, _, fkValue, true) =>
        if (childStore.contains(fkValue)) DuplicateTask else fkt
      case fkt@FkTask(_, _, fkValue, _) =>
        if (parentStore.contains(fkValue) || childStore.contains(fkValue)) DuplicateTask else fkt
      case OriginDbResult(table, rows, fetchChildren) =>
        val pkOrdinals = pkOrdinalsByTable(request.table)
        val pkOrdinal = pkOrdinals.head
        val isSingleColPk = pkOrdinals.size == 1
        val getPkValue: Row => AnyRef = if (isSingleColPk) row => row(pkOrdinal) else row => pkOrdinals.map(row)

        if (fetchChildren) {
          val childrenNotYetFetched = rows.filter(row => childStore.add(getPkValue(row)))
          val parentsNotYetFetched = childrenNotYetFetched.filterNot(row => parentStore.remove(getPkValue(row)))
          PksAdded(table, parentsNotYetFetched, childrenNotYetFetched)
        } else {
          val newRows = rows.filter { row =>
            val pkValue = getPkValue(row)
            !childStore.contains(pkValue) && parentStore.add(pkValue)
          }
          PksAdded(table, newRows, Vector.empty)
        }
    }
  }
}