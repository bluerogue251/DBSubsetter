package trw.dbsubsetter.workflow

import trw.dbsubsetter.db.{SchemaInfo, _}

object RowsToTasksConverter {
  def convert(table: Table, rows: Vector[Row], sch: SchemaInfo, fetchChildren: Boolean): Vector[FkTask] = {
    val parentTasks = for {
      row <- rows
      fk <- sch.fksFromTable(table)
      cols = fk.fromCols
      values = cols.map(c => row(c.name)) if !values.contains(null)
    } yield FkTask(fk.toTable, fk, values, fetchChildren = false)

    val childTasks = if (!fetchChildren) {
      Seq.empty
    } else for {
      row <- rows
      fk <- sch.fksToTable(table)
      cols = fk.toCols
      values = cols.map(c => row(c.name)) if !values.contains(null)
    } yield FkTask(fk.fromTable, fk, values, fetchChildren = true)

    // `distinct` is a performance improvement.
    // It prevents us from later on needing to check the pkStore for the same values over and over again
    // Is there a better way to achieve this same performance gain, e.g. by using a HashMap or a Set instead of a Seq?
    parentTasks.distinct ++ childTasks
  }
}