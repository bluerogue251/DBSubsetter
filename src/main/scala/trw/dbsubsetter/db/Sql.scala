package trw.dbsubsetter.db

object Sql {
  def preparedQueryStatementStrings(sch: SchemaInfo): SqlTemplates = {
    val allCombos = for {
      fk <- sch.fks
      table <- Set(fk.fromTable, fk.toTable)
    } yield (fk, table)

    allCombos.map { case (fk, table) =>
      val whereClauseCols = if (table == fk.toTable) fk.toCols else fk.fromCols
      val whereClauseColumnParts = whereClauseCols.map(col => s"${col.fullyQualifiedName} = ?")

      val sqlString = fk match {
        case ForeignKey(_, _, _, None) =>
          val whereClause = whereClauseColumnParts.mkString(" and ")
          makeQueryString(table, whereClause)
        case ForeignKey(fromCols, toCols, _, Some(whereClause)) =>
          val wc = (whereClauseColumnParts :+ whereClause).mkString(" and ")
          val otherTable = if (table == fk.toTable) fk.fromTable else fk.toTable
          val joinClause = fromCols.zip(toCols).map { case (f, t) => s"${f.fullyQualifiedName} = ${t.fullyQualifiedName}" }.mkString(" and ")
          s"""select ${table.fullyQualifiedName}.*
              | from ${table.fullyQualifiedName}
              | inner join $otherTable on $joinClause
              | where $whereClause""".stripMargin

      }
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