package trw.dbsubsetter.workflow

import trw.dbsubsetter.db._

object NewFkTaskWorkflow {
  def process(pksAdded: PksAdded, sch: SchemaInfo): Map[(ForeignKey, Boolean), Seq[FkTask]] = {
    val PksAdded(table, rowsNeedingParentTasks, rowsNeedingChildTasks, viaTableOpt) = pksAdded
    val parentTasks = calcParentTasks(sch, table, rowsNeedingParentTasks, viaTableOpt)
    val childTasks = calcChildTasks(sch, table, rowsNeedingChildTasks)
    parentTasks ++ childTasks
  }

  private def calcParentTasks(sch: SchemaInfo, table: Table, rows: Vector[Row], viaTableOpt: Option[Table]): Map[(ForeignKey, Boolean), Seq[FkTask]] = {
    // Re: `distinct`
    // It is (hopefully) a performance improvement which prevents duplicate tasks from being created
    //
    // Re: `viaTableOpt`
    // If we know that the reason we fetched a row to begin with is that it is the child of some row we've
    // already fetched, then we know that we don't need to go fetch that particular parent row again
    //
    // `distinct` and `viaTableOpt` only apply for calculating parent tasks, not child tasks.
    // Both of these seem necessary for avoiding always needing to store PKs for all parents
    val allFks = sch.fksFromTable(table)
    val useFks = viaTableOpt.fold(allFks)(viaTable => allFks.filterNot(fk => fk.toTable == viaTable))
    useFks.map { fk =>
      val distinctFkValues = getForeignKeyValues(fk, fk.fromCols, rows).distinct
      val tasks = distinctFkValues.map(fkValue => FkTask(fk.toTable, fk, fkValue, fetchChildren = false))
      (fk, false) -> tasks
    }.toMap
  }

  private def calcChildTasks(sch: SchemaInfo, table: Table, rows: Vector[Row]): Map[(ForeignKey, Boolean), Seq[FkTask]] = {
    sch.fksToTable(table).map { fk =>
      val fkValues = getForeignKeyValues(fk, fk.toCols, rows)
      val tasks = fkValues.map(fkValue => FkTask(fk.fromTable, fk, fkValue, fetchChildren = true))
      (fk, true) -> tasks
    }.toMap
  }

  private def getForeignKeyValues(fk: ForeignKey, cols: Vector[Column], rows: Vector[Row]): Vector[Array[Any]] = {
    val ordinalPositions = cols.map(_.ordinalPosition).toArray
    rows.map(row => ordinalPositions.map(row))
  }
}