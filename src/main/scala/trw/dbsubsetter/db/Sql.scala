package trw.dbsubsetter.db

private[db] object Sql {
  def preparedQueryStatementStrings(sch: SchemaInfo): SqlTemplates = {
    val allCombos = for {
      fk <- sch.fksOrdered
      table <- Set(fk.fromTable, fk.toTable)
    } yield (fk, table)

    allCombos.map { case (fk, table) =>
      val whereClauseCols = if (table == fk.toTable) fk.toCols else fk.fromCols
      val whereClause = whereClauseCols.map(col => s"${quoteFullyQualified(col)} = ?").mkString(" and ")
      (fk, table) -> makeQueryString(table, whereClause, sch)
    }.toMap
  }

  def preparedInsertStatementStrings(sch: SchemaInfo): Map[Table, SqlQuery] = {
    sch.tablesByName.map { case (_, table) =>
      val cols = sch.colsByTableOrdered(table)
      val sqlString =
        s"""insert into ${quote(table)}
           |${cols.map(quote).mkString("(", ",", ")")}
           |values ${(1 to cols.size).map(_ => '?').mkString("(", ",", ")")}""".stripMargin
      val sqlStringAccountingForMsSqlServer = if (table.hasSqlServerAutoIncrement) {
        s"SET IDENTITY_INSERT [${table.schema}].[${table.name}] ON;\n" + sqlString
      } else {
        sqlString
      }
      table -> sqlStringAccountingForMsSqlServer
    }
  }

  def makeQueryString(table: Table, whereClause: WhereClause, sch: SchemaInfo): SqlQuery = {
    val selectCols = sch.colsByTableOrdered(table).map(quoteFullyQualified).mkString(", ")
    s"""select $selectCols
       | from ${quote(table)}
       | where $whereClause
       | """.stripMargin
  }

  private def quoteFullyQualified(col: Column): String = {
    s""""${col.table.schema}"."${col.table.name}"."${col.name}""""
  }

  private def quote(col: Column): String = {
    s""""${col.name}""""
  }

  private def quote(table: Table): String = {
    s""""${table.schema}"."${table.name}""""
  }
}
