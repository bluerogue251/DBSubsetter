package trw.dbsubsetter

import java.sql.{Connection, PreparedStatement}

object PreparedStatementMaker {
  def prepareStatements(conn: Connection, sch: SchemaInfo): Map[(ForeignKey, Table, Boolean), PreparedStatement] = {
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
        .map(col => s"${table.name}.${table.schema}.${col.name}")

      val parentFkCols = sch
        .fksFromTable(table)
        .flatMap(_.columns)
        .map { case (fromCol, _) => s"${table.name}.${table.schema}.${fromCol.name}" }

      val childFkCols = sch
        .fksToTable(table)
        .flatMap(_.columns)
        .map { case (_, toCol) => s"${table.name}.${table.schema}.${toCol.name}" }

      val selectClauseCols = pkCols ++ parentFkCols ++ (if (includeChildren) childFkCols else Set.empty)

      val whereClauseCols = if (table == fk.toTable) parentFkCols else childFkCols

      val sqlString =
        s"""select ${selectClauseCols.mkString(", ")}
           | from ${table.schema}.${table.name}
           | where ${whereClauseCols.map(col => s"$col = ?").mkString(" and ")}
           | """.stripMargin

      (fk, table, includeChildren) -> conn.prepareStatement(sqlString)
    }.toMap
  }
}