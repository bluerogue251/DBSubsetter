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

      val selectColumns: Seq[Column] =
        sch.keyColumnsByTableOrdered(table)

      (fk, table) -> makeQueryString(table, selectColumns, whereClause)
    }.toMap
  }

  def queryByPkSqlTemplates(sch: SchemaInfo): PrimaryKeySqlTemplates = {
    sch.pksByTable.flatMap { case (table, primaryKey) =>
      Constants.dataCopyBatchSizes.map(batchSize => {
        val whereClause: String = makeCompositeWhereClause(primaryKey.columns, batchSize)
        val selectColumns: Seq[Column] = sch.dataColumnsByTableOrdered(table)
        (table, batchSize) -> makeQueryString(table, selectColumns, whereClause)
      })
    }
  }

  def insertSqlTemplates(sch: SchemaInfo): Map[Table, SqlQuery] = {
    sch.tablesWithAutoincrementMetadata.map { case TableWithAutoincrementMetadata(table, hasSqlServerAutoIncrement) =>
      val cols =
        sch.dataColumnsByTableOrdered(table)

      val sqlString =
        s"""insert into ${quote(table)}
           |${cols.map(quote).mkString("(", ",", ")")}
           |values ${(1 to cols.size).map(_ => '?').mkString("(", ",", ")")}""".stripMargin

      val sqlStringAccountingForMsSqlServer =
        if (hasSqlServerAutoIncrement) {
          s"SET IDENTITY_INSERT [${table.schema}].[${table.name}] ON;\n" + sqlString
        } else {
          sqlString
        }

      table -> sqlStringAccountingForMsSqlServer
    }.toMap
  }

  def makeQueryString(table: Table, selectColumns: Seq[Column], whereClause: WhereClause): SqlQuery = {
    val selectClause: String =
      selectColumns.map(quoteFullyQualified).mkString(", ")

    s"""select $selectClause
       | from ${quote(table)}
       | where $whereClause
       | """.stripMargin
  }

  private def makeSimpleWhereClause(columns: Seq[Column]): String = {
    columns
      .map(col => s"${quoteFullyQualified(col)} = ?")
      .mkString(" and ")
  }

  private def makeCompositeWhereClause(columns: Seq[Column], batchSize: Short): String = {
    (1 to batchSize)
      .map(_ => makeSimpleWhereClause(columns))
      .map(simpleWhereClause => s"($simpleWhereClause)")
      .mkString(" or ")
  }

  private def quoteFullyQualified(col: Column): String = {
    s""""${col.table.schema.name}"."${col.table.name}"."${col.name}""""
  }

  private def quote(col: Column): String = {
    s""""${col.name}""""
  }

  private def quote(table: Table): String = {
    s""""${table.schema.name}"."${table.name}""""
  }
}
