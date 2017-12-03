package trw.dbsubsetter.workflow

import trw.dbsubsetter.db._

object NewFkTaskWorkflow {
  def process(pksAdded: PksAdded, sch: SchemaInfo): Vector[FkTask] = {
    val PksAdded(table, rowsNeedingParentTasks, rowsNeedingChildTasks, viaTableOpt) = pksAdded
    val parentTasks = calcParentTasks(sch, table, rowsNeedingParentTasks, viaTableOpt)
    val childTasks = calcChildTasks(sch, table, rowsNeedingChildTasks)
    parentTasks ++ childTasks
  }

  private def calcParentTasks(sch: SchemaInfo, table: Table, rows: Vector[Row], viaTableOpt: Option[Table]): Vector[FkTask] = {
    // Re: `distinct`
    // It is (hopefully) a performance improvement which prevents duplicate tasks from being created
    //
    // Re: `viaTableOpt`
    // If we know that the reason we fetched a row to begin with is that it is the child of some row we've
    // already fetched, then we know that we don't need to go fetch that particular parent row again
    //
    // `distinct` and `viaTableOpt` only apply for calculating parent tasks, not child tasks.
    // Both of these seem necessary for avoiding always needing to store PKs for all parents of base queries
    sch.fksFromTable(table).filterNot(fk => viaTableOpt.contains(fk.toTable)).toVector.flatMap { fk =>
      val distinctFkValues = getForeignKeyValues(fk, fk.fromCols, rows).distinct
      distinctFkValues.map(fkValue => FkTask(fk.toTable, fk, fkValue, fetchChildren = false))
    }
  }

  private def calcChildTasks(sch: SchemaInfo, table: Table, rows: Vector[Row]): Vector[FkTask] = {
    sch.fksToTable(table).toVector.flatMap { fk =>
      val fkValues = getForeignKeyValues(fk, fk.toCols, rows)
      fkValues.map(fkValue => FkTask(fk.fromTable, fk, fkValue, fetchChildren = true))
    }
  }

  private def getForeignKeyValues(fk: ForeignKey, cols: Vector[Column], rows: Vector[Row]): Vector[AnyRef] = {
    val ordinalPositions = cols.map(_.ordinalPosition)
    if (fk.isSingleCol) {
      val ordinalPosition = ordinalPositions.head
      rows.map(row => row(ordinalPosition))
    } else {
      rows.map(row => ordinalPositions.map(row))
    }
  }
}