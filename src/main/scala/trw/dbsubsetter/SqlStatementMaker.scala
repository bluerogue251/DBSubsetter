package trw.dbsubsetter

object SqlStatementMaker {
  def prepareStatementStrings(sch: SchemaInfo): SqlTemplates = {
    val allCombos = for {
      fk <- sch.fks
      table <- Set(fk.fromTable, fk.toTable)
      includeChildren <- Set(true, false)
    } yield (fk, table, includeChildren)

    allCombos.map { case (fk, table, includeChildren) =>
      val whereClauseCols = if (table == fk.toTable) fk.toCols else fk.fromCols
      val whereClause = s"${whereClauseCols.map(col => s"${col.fullyQualifiedName} = ?").mkString(" and ")}"
      val (sqlString, selectClauseCols) = makeSqlString(table, whereClause, sch, includeChildren)
      (fk, table, includeChildren) -> (sqlString, selectClauseCols)
    }.toMap
  }

  def makeSqlString(table: Table, whereClause: WhereClause, sch: SchemaInfo, includeChildren: Boolean): (SqlQuery, Seq[Column]) = {
    val pkCols = sch.pksByTable(table).columns
    val parentFkCols = sch.fksFromTable(table).flatMap(_.fromCols)
    val childFkCols = sch.fksToTable(table).flatMap(_.toCols)
    val selectClauseCols = pkCols ++ parentFkCols ++ (if (includeChildren) childFkCols else Set.empty)

    val sqlString =
      s"""select ${selectClauseCols.map(_.fullyQualifiedName).mkString(", ")}
         | from ${table.fullyQualifiedName}
         | where $whereClause
         | """.stripMargin

    (sqlString, selectClauseCols)
  }
}