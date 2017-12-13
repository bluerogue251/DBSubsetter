package trw.dbsubsetter.workflow

import trw.dbsubsetter.db._

object NewFkTaskWorkflow {
  def process(pksAdded: PksAdded, sch: SchemaInfo): Map[(ForeignKey, Boolean), Vector[Any]] = {
    val PksAdded(table, rowsNeedingParentTasks, rowsNeedingChildTasks, viaTableOpt) = pksAdded
    val parentTasks = calcParentTasks(sch, table, rowsNeedingParentTasks, viaTableOpt)
    val childTasks = calcChildTasks(sch, table, rowsNeedingChildTasks)
    parentTasks ++ childTasks
  }

  private def calcParentTasks(sch: SchemaInfo, table: Table, rows: Vector[Row], viaTableOpt: Option[Table]): Map[(ForeignKey, Boolean), Vector[Any]] = {
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
      val distinctFkValues = valuesFunc(fk)(fk, fk.fromCols, rows).distinct
      (fk, false) -> distinctFkValues
    }.toMap
  }

  private def calcChildTasks(sch: SchemaInfo, table: Table, rows: Vector[Row]): Map[(ForeignKey, Boolean), Vector[Any]] = {
    sch.fksToTable(table).map { fk =>
      val fkValues = valuesFunc(fk)(fk, fk.toCols, rows)
      (fk, true) -> fkValues
    }.toMap
  }

  private def valuesFunc(fk: ForeignKey) = if (fk.isSingleCol) getSingleColForeignKeyValues _ else getMultiColForeignKeyValues _

  private def getSingleColForeignKeyValues(fk: ForeignKey, cols: Vector[Column], rows: Vector[Row]): Vector[Any] = {
    val ordinalPosition = cols.head.ordinalPosition
    rows.map(row => row(ordinalPosition))
  }

  private def getMultiColForeignKeyValues(fk: ForeignKey, cols: Vector[Column], rows: Vector[Row]): Vector[Array[Any]] = {
    val ordinalPositions = cols.map(_.ordinalPosition).toArray
    rows.map(row => ordinalPositions.map(row))
  }
}