package trw.dbsubsetter

import java.sql.Connection

object Processor {
  def process(task: Task, sch: SchemaInfo, conn: Connection, pkStore: PrimaryKeyStore): Seq[Task] = {
    val Task(schema, table, whereClause, fetchChildren) = task

    // Figure out which columns we need to include in the SQL `SELECT` statement
    // So that we don't select any more data than is absolutely necessary
    val pkColumnsToSelect = sch.pksByTable((schema, table)).columns
    val parentFks = sch.fksFromTable((schema, table))
    val childFks = sch.fksToTable((schema, table))
    val parentFkColsToSelect = parentFks.flatMap(_.columns).map { case (fromCol, _) => fromCol }
    val childFkColsToSelect = if (fetchChildren) childFks.flatMap(_.columns).map { case (_, toCol) => toCol } else Set.empty
    val columnsToSelect: Seq[Column] = pkColumnsToSelect ++ parentFkColsToSelect ++ childFkColsToSelect

    // Build and execute the SQL statement to select the data matching the where clause
    val query =
      s"""select ${columnsToSelect.map(_.name).mkString(", ")}
         | from $schema.$table
         | where $whereClause
         | """.stripMargin

    // Find out which rows are "new" in the sense of having not yet been processed by us
    // Add the primary key of each of the "new" rows to the primaryKeyStore.
    val allMatchingRows = DbAccess.getRows(conn, query, columnsToSelect)
    val newRows = allMatchingRows.filter(row => pkStore((schema, table)).add(pkColumnsToSelect.map(k => row(k.name))))

    val parentTasks = newRows.flatMap { row =>
      parentFks.flatMap { pfk =>
        val whereClause = pfk.columns.flatMap { case (fromCol, toCol) =>
          Option(row(fromCol.name)).map(fromColValue => s"${toCol.name} = '$fromColValue'")
        }.mkString(" and ")

        if (whereClause.isEmpty)
          None
        else
          Some(Task(pfk.toSchema, pfk.toTable, whereClause, false))
      }
    }

    val childTasks = {
      if (!fetchChildren) {
        Seq.empty[Task]
      } else {
        newRows.flatMap { row =>
          childFks.flatMap { cfk =>
            val whereClause = cfk.columns.flatMap { case (fromCol, toCol) =>
              Option(row(toCol.name)).map(toColValue => s"${fromCol.name} = '$toColValue'")
            }.mkString(" and ")

            if (whereClause.isEmpty)
              None
            else
              Some(Task(cfk.fromSchema, cfk.fromTable, whereClause, true))
          }
        }
      }
    }

    parentTasks ++ childTasks
  }
}