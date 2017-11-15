package trw.dbsubsetter.db

object Sql {
  def preparedQueryStatementStrings(sch: SchemaInfo): SqlTemplates = {
    val allCombos = for {
      fk <- sch.fks
      table <- Set(fk.fromTable, fk.toTable)
    } yield (fk, table)

    allCombos.map { case (fk, table) =>
      val whereClauseCols = if (table == fk.toTable) fk.toCols else fk.fromCols
      val whereClause = s"${whereClauseCols.map(col => s"${col.fullyQualifiedName} = ?").mkString(" and ")}"
      val sqlString = makeQueryString(table, whereClause)
      (fk, table) -> sqlString
    }.toMap
  }

  def preparedInsertStatementStrings(sch: SchemaInfo): Map[Table, SqlQuery] = {
    sch.tablesByName.map { case (tableName, table) =>
      val cols = sch.colsByTable(table)
      val sqlString =
        s"""insert into ${table.fullyQualifiedName}
           |${cols.map(_.quotedName).mkString("(", ",", ")")}
           |values ${(1 to cols.size).map(_ => '?').mkString("(", ",", ")")}""".stripMargin
      table -> sqlString
    }
  }

  def makeQueryString(table: Table, whereClause: WhereClause): SqlQuery = {
    s"""select ${table.fullyQualifiedName}.*
         | from ${table.fullyQualifiedName}
         | where $whereClause
         | """.stripMargin
  }
}