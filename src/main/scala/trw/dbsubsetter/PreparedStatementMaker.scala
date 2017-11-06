package trw.dbsubsetter

import java.sql.{Connection, PreparedStatement}

object PreparedStatementMaker {
  def prepareStatements(conn: Connection, sch: SchemaInfo): Map[(ForeignKey, Table, Boolean), (PreparedStatement, Seq[Column])] = {
    val allCombos = for {
      fk <- sch.fks
      table <- Set(fk.fromTable, fk.toTable)
      includeChildren <- Set(true, false)
    } yield (fk, table, includeChildren)

    // Need to DRY up the building of fully-qualified column names (schema.table.column)
    allCombos.map { case (fk, table, includeChildren) =>
      val pkCols = sch
        .pksByTable(table)
        .columns

      val parentFkCols = sch
        .fksFromTable(table)
        .flatMap(_.fromCols)

      val childFkCols = sch
        .fksToTable(table)
        .flatMap(_.toCols)

      val selectClauseCols = pkCols ++ parentFkCols ++ (if (includeChildren) childFkCols else Set.empty)
      val selectClauseColNames = selectClauseCols.map(col => s"${table.schema}.${table.name}.${col.name}")

      val whereClauseCols = if (table == fk.toTable) fk.toCols else fk.fromCols
      val whereClauseColNames = whereClauseCols.map(col => s"${table.schema}.${table.name}.${col.name}")

      val sqlString =
        s"""select ${selectClauseColNames.mkString(", ")}
           | from ${table.schema}.${table.name}
           | where ${whereClauseColNames.map(col => s"$col = ?").mkString(" and ")}
           | """.stripMargin

      (fk, table, includeChildren) -> (conn.prepareStatement(sqlString), selectClauseCols)
    }.toMap
  }
}