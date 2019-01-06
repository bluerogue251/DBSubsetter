package trw.dbsubsetter.workflow

import trw.dbsubsetter.db.{Row, SchemaInfo, Table}
import trw.dbsubsetter.primarykeystore.{AlreadySeenWithoutChildren, FirstTimeSeen, PrimaryKeyStore, WriteOutcome}


final class PkStoreWorkflow(pkStore: PrimaryKeyStore, schemaInfo: SchemaInfo) {

  private[this] val pkValueExtractionFunctions: Map[Table, Row => Any] =
    PkStoreWorkflow.buildPkValueExtractionFunctions(schemaInfo)

  def add(req: OriginDbResult): PksAdded = {
    val OriginDbResult(table, rows, viaTableOpt, fetchChildren) = req
    val pkValueExtractionFunction: Row => Any = pkValueExtractionFunctions(table)

    if (fetchChildren) {
      val outcomes: Vector[(WriteOutcome, Row)] = rows.map(row => {
        val pkValue: Any = pkValueExtractionFunction(row)
        val outcome: WriteOutcome = pkStore.markSeenWithChildren(table, pkValue)
        outcome -> row
      })

      val outcomeMap: Map[WriteOutcome, Vector[Row]] =
        outcomes.foldLeft(PkStoreWorkflow.EmptyMap) { case (map, (outcome, row)) =>
          map.updated(outcome, map(outcome) :+ row)
        }

      val parentsNotYetFetched: Vector[Row] = outcomeMap(FirstTimeSeen)
      val childrenNotYetFetched: Vector[Row] = parentsNotYetFetched ++ outcomeMap(AlreadySeenWithoutChildren)

      PksAdded(table, parentsNotYetFetched, childrenNotYetFetched, viaTableOpt)
    } else {
      val newRows = rows.filter(row => {
        val pkValue: Any = pkValueExtractionFunction(row)
        pkStore.markSeen(table, pkValue) match {
          case FirstTimeSeen => true
          case _ => false
        }
      })
      PksAdded(table, newRows, Vector.empty, viaTableOpt)
    }
  }
}

private[this] object PkStoreWorkflow {

  private val EmptyMap: Map[WriteOutcome, Vector[Row]] =
    Map.empty[WriteOutcome, Vector[Row]]
      .withDefaultValue(Vector.empty[Row])

  // Consider putting this logic as a field inside the `Table` class itself
  private def buildPkValueExtractionFunctions(schemaInfo: SchemaInfo): Map[Table, Row => Any] = {
    schemaInfo.pksByTableOrdered.map { case (table, pkColumns) =>
        val pkOrdinals: Vector[Int] = pkColumns.map(_.ordinalPosition)
        val isSingleColPk: Boolean = pkOrdinals.lengthCompare(1) == 0
        val function: Row => Any = if (isSingleColPk) row => row(pkOrdinals.head) else row => pkOrdinals.map(row)
        table -> function
    }
  }
}