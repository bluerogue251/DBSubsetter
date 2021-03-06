package trw.dbsubsetter.pkstore

import trw.dbsubsetter.OriginDbResult
import trw.dbsubsetter.db.{Keys, PrimaryKeyValue, SchemaInfo, Table}
import trw.dbsubsetter.keyextraction.KeyExtractionUtil

final class PkStoreWorkflow(multiTablePkStore: MultiTablePkStore, schemaInfo: SchemaInfo) {

  private[this] val EmptyMap: Map[WriteOutcome, Vector[Keys]] =
    Map
      .empty[WriteOutcome, Vector[Keys]]
      .withDefaultValue(Vector.empty[Keys])

  private[this] val pkValueExtractionFunctions: Map[Table, Keys => PrimaryKeyValue] =
    KeyExtractionUtil.pkExtractionFunctions(schemaInfo)

  def alreadySeen(table: Table, primaryKeyValue: PrimaryKeyValue): Boolean = {
    multiTablePkStore.alreadySeen(table, primaryKeyValue)
  }

  def add(req: OriginDbResult): PksAdded = {
    val OriginDbResult(table, rows, viaTableOpt, fetchChildren) = req
    val pkValueExtractionFunction: Keys => PrimaryKeyValue = pkValueExtractionFunctions(table)

    if (fetchChildren) {
      val outcomes: Vector[(WriteOutcome, Keys)] = rows.map(row => {
        val pkValue: PrimaryKeyValue = pkValueExtractionFunction(row)
        val outcome: WriteOutcome = multiTablePkStore.markSeenWithChildren(table, pkValue)
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
      val newRows = rows.filter(row => {
        val pkValue: PrimaryKeyValue = pkValueExtractionFunction(row)
        multiTablePkStore.markSeen(table, pkValue) match {
          case FirstTimeSeen => true
          case _             => false
        }
      })
      PksAdded(table, newRows, Vector.empty, viaTableOpt)
    }
  }
}

object PkStoreWorkflow {
  def from(schemaInfo: SchemaInfo): PkStoreWorkflow = {
    val multiTablePkStore: MultiTablePkStore = MultiTablePkStore.from(schemaInfo.pksByTable.keys.toSeq)
    new PkStoreWorkflow(multiTablePkStore, schemaInfo)
  }
}
