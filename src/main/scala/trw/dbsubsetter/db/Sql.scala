package trw.dbsubsetter.db

private[db] object Sql {
  def queryByFkSqlTemplates(sch: SchemaInfo): ForeignKeySqlTemplates = {
    val allCombos = for {
      fk <- sch.fksOrdered
      table <- Set(fk.fromTable, fk.toTable)
    } yield (fk, table)

    allCombos.map { case (fk, table) =>
      val whereClauseColumns: Seq[Column] =
        if (table == fk.toTable) {
          fk.toCols
        } else {
          fk.fromCols
        }

      val whereClause: String =
        makeSimpleWhereClause(whereClauseColumns)

      (fk, table) -> makeQueryString(table, whereClause, sch)
    }.toMap
  }

  def queryByPkSqlTemplates(sch: SchemaInfo): PrimaryKeySqlTemplates = {
    sch.pksByTableOrdered.flatMap { case (table, primaryKeyColumns) =>
      Constants.dataCopyBatchSizes.map(batchSize => {
        val whereClause: String = makeCompositeWhereClause(primaryKeyColumns, batchSize)
        (table, batchSize) -> makeQueryString(table, whereClause, sch)
      })
    }
  }

  def insertSqlTemplates(sch: SchemaInfo): Map[Table, SqlQuery] = {
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

  private def makeSimpleWhereClause(columns: Seq[Column]): String = {
    columns
      .map(col => s"${quoteFullyQualified(col)} = ?")
      .mkString(" and ")
  }

  private def makeCompositeWhereClause(columns: Seq[Column], batchSize: Int): String = {
    (1 to batchSize)
      .map(_ => makeSimpleWhereClause(columns))
      .map(simpleWhereClause => s"($simpleWhereClause)")
      .mkString(" or ")
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
