package trw.dbsubsetter.workflow

import trw.dbsubsetter.db._

object NewFkTaskWorkflow {
  def process(pksAdded: PksAdded, sch: SchemaInfo): Vector[FkTask] = {
    val PksAdded(table, rowsNeedingParentTasks, rowsNeedingChildTasks) = pksAdded
    val parentTasks = calcParentTasks(sch, table, rowsNeedingParentTasks)
    val childTasks = calcChildTasks(sch, table, rowsNeedingChildTasks)
    parentTasks ++ childTasks
  }

  private def calcParentTasks(sch: SchemaInfo, table: Table, rows: Vector[Row]): Vector[FkTask] = {
    // `distinct` is (hopefully) a performance improvement which prevents duplicate tasks from being created
    // As far as I can tell, it is only necessary for parent tasks
    sch.fksFromTable(table).toVector.flatMap { fk =>
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
    val ordinalPositions = cols.map(_.ordinalPosition - 1)
    if (fk.isSingleCol) {
      val ordinalPosition = ordinalPositions.head
      rows.map(row => row(ordinalPosition))
    } else {
      rows.map(row => ordinalPositions.map(row))
    }
  }
}