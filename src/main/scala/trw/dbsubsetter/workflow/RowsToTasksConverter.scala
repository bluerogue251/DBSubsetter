package trw.dbsubsetter.workflow

import trw.dbsubsetter.db.{SchemaInfo, _}

object RowsToTasksConverter {
  def convert(table: Table, rows: Vector[Row], sch: SchemaInfo, fetchChildren: Boolean): Vector[FkTask] = {
    // `distinct` is a performance improvement which prevents duplicate tasks from being created
    // As far as I can tell, it is only necessary for parent tasks
    val parentTasks = sch.fksFromTable(table).toVector.flatMap { fk =>
      val distinctFkValues = rows.map(row => fk.fromCols.map(_.ordinalPosition - 1).map(row)).distinct
      distinctFkValues.map(fkValue => FkTask(fk.toTable, fk, fkValue, fetchChildren = false))
    }

    val childTasks = if (fetchChildren) {
      sch.fksToTable(table).toVector.flatMap { fk =>
        val fkValues = rows.map(row => fk.toCols.map(_.ordinalPosition - 1).map(row))
        fkValues.map(fkValue => FkTask(fk.fromTable, fk, fkValue, fetchChildren = true))
      }
    } else {
      Vector.empty
    }

    parentTasks ++ childTasks
  }
}