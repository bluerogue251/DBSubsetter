package trw.dbsubsetter.workflow

import trw.dbsubsetter.db.{PrimaryKeyValue, Row, SchemaInfo, Table}
import trw.dbsubsetter.pkvalueextraction.PkValueExtractionUtil
import trw.dbsubsetter.primarykeystore.{AlreadySeenWithoutChildren, FirstTimeSeen, PrimaryKeyStore, WriteOutcome}


final class PkStoreWorkflow(pkStore: PrimaryKeyStore, schemaInfo: SchemaInfo) {

  private[this] val pkValueExtractionFunctions: Map[Table, Row => PrimaryKeyValue] =
    PkValueExtractionUtil.pkValueExtractionFunctionsByTable(schemaInfo)

  def add(req: OriginDbResult): PksAdded = {
    val OriginDbResult(table, rows, viaTableOpt, fetchChildren) = req
    val pkValueExtractionFunction: Row => PrimaryKeyValue = pkValueExtractionFunctions(table)

    if (fetchChildren) {
      val outcomes: Vector[(WriteOutcome, Row)] = rows.map(row => {
        val pkValue: PrimaryKeyValue = pkValueExtractionFunction(row)
        val outcome: WriteOutcome = pkStore.markSeenWithChildren(table, pkValue)
        outcome -> row
      })

      val outcomeMap: Map[WriteOutcome, Vector[Any]] =
        outcomes.foldLeft(PkStoreWorkflow.EmptyMap) { case (map, (outcome, row)) =>
          map.updated(outcome, map(outcome) :+ row)
        }

      val parentsNotYetFetched: Vector[Any] = outcomeMap(FirstTimeSeen)
      val childrenNotYetFetched: Vector[Any] = parentsNotYetFetched ++ outcomeMap(AlreadySeenWithoutChildren)

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
}
