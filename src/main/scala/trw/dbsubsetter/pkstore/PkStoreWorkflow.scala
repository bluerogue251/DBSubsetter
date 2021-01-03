package trw.dbsubsetter.pkstore

import trw.dbsubsetter.OriginDbResult
import trw.dbsubsetter.db.{Keys, PrimaryKeyValue, SchemaInfo, Table}

final class PkStoreWorkflow(multiTablePkStore: MultiTablePkStore) {

  private[this] val EmptyMap: Map[WriteOutcome, Vector[Keys]] =
    Map
      .empty[WriteOutcome, Vector[Keys]]
      .withDefaultValue(Vector.empty[Keys])

  def alreadySeen(table: Table, primaryKeyValue: PrimaryKeyValue): Boolean = {
    multiTablePkStore.alreadySeen(table, primaryKeyValue)
  }

  def add(req: OriginDbResult): PksAdded = {
    val OriginDbResult(table, rows, viaTableOpt, fetchChildren) = req

    if (fetchChildren) {
      val outcomes: Vector[(WriteOutcome, Keys)] = rows.map(row => {
        val outcome: WriteOutcome = multiTablePkStore.markSeenWithChildren(table, row.pkValue)
        outcome -> row
      })

      val outcomeMap: Map[WriteOutcome, Vector[Keys]] =
        outcomes.foldLeft(EmptyMap) { case (map, (outcome, row)) =>
          map.updated(outcome, map(outcome) :+ row)
        }

      val parentsNotYetFetched: Vector[Keys] = outcomeMap(FirstTimeSeen)
      val childrenNotYetFetched: Vector[Keys] = parentsNotYetFetched ++ outcomeMap(AlreadySeenWithoutChildren)

      PksAdded(table, parentsNotYetFetched, childrenNotYetFetched, viaTableOpt)
    } else {
      val newRows =
        rows.filter { row =>
          multiTablePkStore.markSeen(table, row.pkValue) == FirstTimeSeen
        }
      PksAdded(table, newRows, Vector.empty, viaTableOpt)
    }
  }
}

object PkStoreWorkflow {
  def from(schemaInfo: SchemaInfo): PkStoreWorkflow = {
    val multiTablePkStore: MultiTablePkStore = MultiTablePkStore.from(schemaInfo.pksByTable.keys.toSeq)
    new PkStoreWorkflow(multiTablePkStore)
  }
}
