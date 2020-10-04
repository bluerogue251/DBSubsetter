package trw.dbsubsetter.db

import trw.dbsubsetter.config.{ConfigColumn, SchemaConfig}

object SchemaValidation {

  def validate(schemaConfig: SchemaConfig, dbMeta: DbMetadataQueryResult): SchemaValidationResult = {
    val actualSchemas: Set[Schema] =
      dbMeta.schemas
        .map(schemaQueryRow => Schema(schemaQueryRow.name))
        .toSet

    /*
     * Detect Missing Schemas
     */
    val missingSchemas: Set[Schema] = schemaConfig.schemas.filterNot(actualSchemas)
    if (missingSchemas.nonEmpty) {
      val csv = missingSchemas.map(_.name).mkString(", ")
      return ValidationError(s"Specified --schemas not found in database: $csv")
    }

    val actualTables: Set[Table] =
      dbMeta.tables
        .map(tableQueryRow => Table(Schema(tableQueryRow.schema), tableQueryRow.name))
        .toSet

    /*
     * Detect missing tables from base queries
     */
    schemaConfig.baseQueries.foreach { baseQuery =>
      if (!actualTables.contains(baseQuery.table)) {
        return missingTable("--baseQuery", baseQuery.table)
      }
    }

    /*
     * Detect missing tables from --primaryKey
     */
    schemaConfig.extraPrimaryKeys.foreach { extraPrimaryKey =>
      if (!actualTables.contains(extraPrimaryKey.table)) {
        return missingTable("--primaryKey", extraPrimaryKey.table)
      }
    }

    /*
     * Detect missing tables from --foreignKey
     */
    schemaConfig.extraForeignKeys.foreach { extraForeignKey =>
      if (!actualTables.contains(extraForeignKey.fromTable)) {
        return missingTable("--foreignKey", extraForeignKey.fromTable)
      }
      if (!actualTables.contains(extraForeignKey.toTable)) {
        return missingTable("--foreignKey", extraForeignKey.toTable)
      }
    }

    /*
     * Detect missing tables from --excludeTable
     */
    schemaConfig.excludeTables.foreach { excludeTable =>
      if (!actualTables.contains(excludeTable)) {
        return missingTable("--excludeTable", excludeTable)
      }
    }

    /*
     * Detect missing tables from --excludeColumns
     */
    schemaConfig.excludeColumns.foreach { excludeColumn =>
      if (!actualTables.contains(excludeColumn.table)) {
        return missingTable("--excludeColumns", excludeColumn.table)
      }
    }

    val tablesWithPrimaryKeysInDb: Set[Table] =
      dbMeta.primaryKeyColumns
        .map({ pkColumnQueryRow =>
          val schema = Schema(pkColumnQueryRow.schema)
          Table(schema, pkColumnQueryRow.table)
        })
        .toSet

    /*
     * Detect duplicate primary key specifications
     */
    schemaConfig.extraPrimaryKeys.foreach { extraPk =>
      if (tablesWithPrimaryKeysInDb.contains(extraPk.table)) {
        return ValidationError(
          s"--primaryKey specified for table ${display(extraPk.table)} which already has a primary key"
        )
      }
    }

    /*
     * Detect missing primary keys
     */
    val tablesWithConfiguredPrimaryKeys: Set[Table] = schemaConfig.extraPrimaryKeys.map(_.table)
    val allTablesWithPrimaryKeys = tablesWithPrimaryKeysInDb ++ tablesWithConfiguredPrimaryKeys
    val tablesMissingPrimaryKeys = actualTables -- allTablesWithPrimaryKeys
    if (tablesMissingPrimaryKeys.size == 1) {
      val table = tablesMissingPrimaryKeys.head
      return ValidationError(
        s"""Table ${display(table)} is missing a primary key. Consider either:
           |  (a) Specifying the missing primary key with the --primaryKey command line option
           |  (b) Excluding the table with the --excludeTable command line option
           |  (c) Adding a primary key onto the table in your origin database
           |""".stripMargin
      )
    } else if (tablesMissingPrimaryKeys.size > 1) {
      val tablesCsv = tablesMissingPrimaryKeys.map(display).mkString(", ")
      return ValidationError(
        s"""Tables $tablesCsv are missing primary keys. Consider either:
           |  (a) Specifying the missing primary keys with the --primaryKey command line option
           |  (b) Excluding the tables with the --excludeTable command line option
           |  (c) Adding a primary key onto the tables in your origin database
           |""".stripMargin
      )
    }

    val actualColumns: Set[ConfigColumn] =
      dbMeta.columns
        .map(columnQueryRow => {
          val schema = Schema(columnQueryRow.schema)
          val table = Table(schema, columnQueryRow.table)
          ConfigColumn(table, columnQueryRow.name)
        })
        .toSet

    /*
     * Detect missing columns from --primaryKey
     */
    schemaConfig.extraPrimaryKeys
      .flatMap(_.columns)
      .foreach { primaryKeyColumn =>
        if (!actualColumns.contains(primaryKeyColumn)) {
          return missingColumn("--primaryKey", primaryKeyColumn)
        }
      }

    /*
     * Detect missing columns from --foreignKey
     */
    schemaConfig.extraForeignKeys
      .flatMap(fk => fk.fromColumns ++ fk.toColumns)
      .foreach { foreignKeyColumn =>
        if (!actualColumns.contains(foreignKeyColumn)) {
          return missingColumn("--foreignKey", foreignKeyColumn)
        }
      }

    /*
     * Detect missing columns from --excludeColumns
     */
    schemaConfig.excludeColumns
      .foreach { excludeColumn =>
        if (!actualColumns.contains(excludeColumn)) {
          return missingColumn("--excludeColumns", excludeColumn)
        }
      }

    OK
  }

  private def missingTable(option: String, table: Table): ValidationError = {
    ValidationError(s"Table ${display(table)} specified in $option not found in database")
  }

  private def missingColumn(option: String, column: ConfigColumn): ValidationError = {
    ValidationError(s"Column ${display(column)} specified in $option not found in database")
  }

  private def display(table: Table): String = {
    s"'${table.schema.name}.${table.name}'"
  }

  private def display(column: ConfigColumn): String = {
    s"'${column.table.schema.name}.${column.table.name}.${column.name}'"
  }
}

sealed trait SchemaValidationResult
case object OK extends SchemaValidationResult
case class ValidationError(message: String) extends SchemaValidationResult
