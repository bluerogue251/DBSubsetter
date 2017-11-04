package trw.dbsubsetter

import java.sql.{Connection, PreparedStatement}

object Processor {
  def process(task: Task, sch: SchemaInfo, conn: Connection, pkStore: PrimaryKeyStore, preparedStatements: Map[(ForeignKey, Table, Boolean), (PreparedStatement, Seq[Column])]): Seq[Task] = {
    val Task(table, fk, values, fetchChildren) = task

    val (stmt, selectCols) = preparedStatements((fk, table, fetchChildren))

    // Find out which rows are "new" in the sense of having not yet been processed by us
    // Add the primary key of each of the "new" rows to the primaryKeyStore.
    val allMatchingRows = DbAccess.getRows(stmt, values, selectCols)
    val newRows = allMatchingRows.filter { row =>
      pkStore(table).add(selectCols.map(row).toVector)
    }

    val parentTasks = for {
      row <- newRows
      fk <- sch.fksFromTable(table)
      cols = fk.columns.map { case (from, _) => from }
      values = cols.map(row)
    } yield Task(fk.toTable, fk, values, false)

    val childTasks = if (!fetchChildren) Seq.empty else for {
      row <- newRows
      fk <- sch.fksToTable(table)
      cols = fk.columns.map { case (_, to) => to }
      values = cols.map(row)
    } yield Task(fk.fromTable, fk, values, true)

    parentTasks ++ childTasks
  }
}