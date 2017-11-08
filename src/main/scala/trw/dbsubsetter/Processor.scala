package trw.dbsubsetter

import java.sql.{Connection, PreparedStatement}

object Processor {
  def process(task: Task, sch: SchemaInfo, conn: Connection, pkStore: PrimaryKeyStore, preparedStatements: Map[(ForeignKey, Table, Boolean), (PreparedStatement, Seq[Column])]): Seq[Task] = {
    val Task(table, fk, values, fetchChildren) = task

    val hasEquivalentAlreadyBeenProcessed = fk.pointsToPk && table == fk.toTable && pkStore(table).contains(values)

    if (hasEquivalentAlreadyBeenProcessed) {
      Seq.empty
    } else {
      val (stmt, selectCols) = preparedStatements((fk, table, fetchChildren))

      // Find out which rows are "new" in the sense of having not yet been processed by us
      // Add the primary key of each of the "new" rows to the primaryKeyStore.
      val allMatchingRows = DbAccess.getRows(stmt, values, selectCols)
      val newRows = allMatchingRows.filter { row =>
        val pkValues = sch.pksByTable(table).columns.map(row)
        pkStore(table).add(pkValues)
      }

      RowsToTasksConverter.convert(table, newRows, sch, fetchChildren)
    }
  }
}