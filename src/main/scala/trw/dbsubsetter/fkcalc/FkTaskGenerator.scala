package trw.dbsubsetter.fkcalc

import trw.dbsubsetter.db._
import trw.dbsubsetter.pkstore.PksAdded

final class FkTaskGenerator(schemaInfo: SchemaInfo) {

  private[this] val fkExtractionFunctions: Map[(ForeignKey, Boolean), Keys => ForeignKeyValue] =
    FkExtractor.fkExtractionFunctions(schemaInfo)

  def generateFrom(pksAdded: PksAdded): IndexedSeq[ForeignKeyTask] = {
    val PksAdded(table, rowsNeedingParentTasks, rowsNeedingChildTasks, viaTableOpt) = pksAdded
    val parentTasks = calcParentTasks(table, rowsNeedingParentTasks, viaTableOpt)
    val childTasks = calcChildTasks(table, rowsNeedingChildTasks)
    parentTasks ++ childTasks
  }

  private[this] def calcParentTasks(
      table: Table,
      rows: Vector[Keys],
      viaTableOpt: Option[Table]
  ): IndexedSeq[ForeignKeyTask] = {
    /*
     * Re: `viaTableOpt`, if we know that the reason we fetched a row to begin with is that it is the child of some row
     * we've already fetched, then we know that we don't need to go fetch that particular parent row again. This only
     * applies for calculating parent tasks, not child tasks. `filterNot(_.isEmpty)` should also only be necessary for
     * parent tasks, not child tasks.
     */
    val allForeignKeys = schemaInfo.fksFromTable(table)
    val useForeignKeys =
      viaTableOpt.fold(allForeignKeys)(viaTable => allForeignKeys.filterNot(fk => fk.toTable == viaTable))
    useForeignKeys.flatMap { foreignKey =>
      val extractionFunction: Keys => ForeignKeyValue = fkExtractionFunctions(foreignKey, false)
      val fkValues: Seq[ForeignKeyValue] = rows.map(extractionFunction).filterNot(_.isEmpty)
      fkValues.map(fkValue => FetchParentTask(foreignKey, fkValue))
    }
  }

  private[this] def calcChildTasks(table: Table, rows: Vector[Keys]): IndexedSeq[ForeignKeyTask] = {
    val allForeignKeys = schemaInfo.fksToTable(table)
    allForeignKeys.flatMap { foreignKey =>
      val extractionFunction: Keys => ForeignKeyValue = fkExtractionFunctions(foreignKey, true)
      val fkValues: Seq[ForeignKeyValue] = rows.map(extractionFunction)
      fkValues.map(fkValue => FetchChildrenTask(foreignKey, fkValue))
    }
  }

}
