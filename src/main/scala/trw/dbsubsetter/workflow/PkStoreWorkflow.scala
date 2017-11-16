package trw.dbsubsetter.workflow

import trw.dbsubsetter.db.Table

import scala.collection.mutable


class PkStoreWorkflow(pkOrdinalsByTable: Map[Table, Seq[Int]]) {
  private val tables = pkOrdinalsByTable.keys
  // Left side of the tuple is for parents, right side of the tuple is for children
  // If a PK is on the children side, then both its children AND its parents have been fetched.
  // IF a PK is on the parent side, then only its parents have been fetched
  // There is no such thing as having fetched a row's children but not having fetched its parents
  // If a PK is in there at all, then at any given time, it is either in the parent set or child set, never both at once.
  // We run the risk of fetching parents for a row more than once, but that is probably not very frequent so we don't try to account for it here.
  private val pkStore: Map[Table, (mutable.HashSet[AnyRef], mutable.HashSet[AnyRef])] = tables.map { t =>
    t -> (mutable.HashSet.empty[AnyRef], mutable.HashSet.empty[AnyRef])
  }.toMap

  def process(request: PkRequest): List[PkResult] = {
    val (parentStore, childStore) = pkStore(request.table)
    request match {
      case fkt@FkTask(_, _, fkValue, true) =>
        if (childStore.contains(fkValue)) List.empty else List(fkt)
      case fkt@FkTask(_, _, fkValue, _) =>
        if (parentStore.contains(fkValue) || childStore.contains(fkValue)) List.empty else List(fkt)
      case OriginDbResult(table, rows, fetchChildren) =>
        lazy val pkOrdinals = pkOrdinalsByTable(request.table)
        lazy val pkOrdinal = pkOrdinals.head
        lazy val isSingleColPk = pkOrdinals.size == 1

        val newRows = rows.filter { row =>
          val pkValue = if (isSingleColPk) row(pkOrdinal) else pkOrdinals.map(row)
          if (fetchChildren) {
            parentStore.remove(pkValue)
            childStore.add(pkValue)
          } else {
            childStore.contains(pkValue) || parentStore.add(pkValue)
          }
        }

        List(PksAdded(table, newRows, fetchChildren))
    }
  }
}