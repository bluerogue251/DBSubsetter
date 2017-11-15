package trw.dbsubsetter.workflow

import trw.dbsubsetter.db.SchemaInfo

import scala.collection.mutable


class PkStoreWorkflow(schemaInfo: SchemaInfo) {
  private val tables = schemaInfo.tablesByName.values
  private val pkStore = tables.map(t => t -> mutable.HashSet.empty[Vector[Any]]).toMap
  private val pkOrdinalsByTable = tables.map(t => t -> schemaInfo.pkColsByTable(t).map(_.ordinalPosition - 1)).toMap

  def process(request: PkRequest): List[PkResult] = {
    request match {
      case fkt@FkTask(table, _, fkValue, _) =>
        if (pkStore(table).contains(fkValue)) List.empty else List(fkt)
      case OriginDbResult(table, rows, fetchChildren) =>
        val ordinals = pkOrdinalsByTable(table)
        val newRows = rows.filter { row =>
          val pkValue = ordinals.map(row)
          pkStore(table).add(pkValue)
        }
        List(PksAdded(table, newRows, fetchChildren))
    }
  }
}
