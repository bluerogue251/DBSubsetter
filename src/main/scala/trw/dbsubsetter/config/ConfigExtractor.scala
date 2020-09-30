package trw.dbsubsetter.config

import trw.dbsubsetter.db.{Schema, Table}

/**
  * Extract command line supplied config values which are simple strings into
  * richer types. If invalid input prevents this, surface this as a validation error.
  */
object ConfigExtractor {
  def extractSchemaConfig(input: CommandLineArgs): SchemaConfigExtractionResult = {
    val schemas = input.schemas.map(Schema)

    val baseQueryRegex = """^\s*(.+)\.(.+)\s+:::\s+(.+)\s+:::\s+(includeChildren|excludeChildren)\s*$""".r
    val baseQueries =
      input.baseQueries
        .map {
          case baseQueryRegex(schemaName, tableName, whereClause, includeChildren) =>
            val table = normalizeTable(schemaName, tableName)
            ConfigBaseQuery(table, whereClause.trim, includeChildren == "includeChildren")
          case baseQueryString =>
            return InvalidInput(InvalidBaseQuery(baseQueryString))
        }

    val foreignKeyRegex = """^(.+)\.(.+)\((.+)\)\s+:::\s+(.+)\.(.+)\((.+)\)\s*$""".r
    val extraForeignKeys =
      input.extraForeignKeys
        .map {
          case foreignKeyRegex(fromSchemaName, fromTableName, fromCols, toSchemaName, toTableName, toCols) =>
            val fromTable = normalizeTable(fromSchemaName, fromTableName)
            val toTable = normalizeTable(toSchemaName, toTableName)
            ConfigForeignKey(
              fromTable = fromTable,
              fromColumns = normalizeColumns(fromTable, fromCols),
              toTable = toTable,
              toColumns = normalizeColumns(toTable, toCols)
            )
          case foreignKeyString =>
            return InvalidInput(InvalidExtraForeignKey(foreignKeyString))
        }

    val primaryKeyRegex = """^\s*(.+)\.(.+)\((.+)\)\s*$""".r
    val extraPrimaryKeys =
      input.extraPrimaryKeys
        .map {
          case primaryKeyRegex(schemaName, tableName, cols) =>
            val table = normalizeTable(schemaName, tableName)
            val columns = normalizeColumns(table, cols)
            ConfigPrimaryKey(table, columns)
          case primaryKeyString =>
            return InvalidInput(InvalidExtraPrimaryKey(primaryKeyString))
        }

    val tableRegex = """^\s*(.+)\.(.+)\s*$""".r
    val excludeTables =
      input.excludeTables
        .map {
          case tableRegex(schemaName, tableName) =>
            normalizeTable(schemaName, tableName)
          case excludeTableString =>
            return InvalidInput(InvalidExcludeTable(excludeTableString))
        }

    val columnRegex = """^\s*(.+)\.(.+)\((.+)\)\s*$""".r
    val excludeColumns =
      input.excludeColumns
        .flatMap {
          case columnRegex(schemaName, tableName, cols) =>
            val table = normalizeTable(schemaName, tableName)
            normalizeColumns(table, cols).toSet
          case excludeColumnsString =>
            return InvalidInput(InvalidExcludeTable(excludeColumnsString))
        }

    Valid(
      SchemaConfig(
        schemas = schemas,
        baseQueries = baseQueries,
        extraForeignKeys = extraForeignKeys,
        extraPrimaryKeys = extraPrimaryKeys,
        excludeTables = excludeTables,
        excludeColumns = excludeColumns
      )
    )
  }

  private[this] def normalizeTable(schemaName: String, tableName: String): Table = {
    val schema = Schema(schemaName.trim)
    Table(schema = schema, name = tableName.trim)
  }

  private[this] def normalizeColumns(table: Table, untrimmedColumnCsvs: String): Seq[ConfigColumn] = {
    untrimmedColumnCsvs
      .split(",")
      .map(_.trim)
      .map(columnName => ConfigColumn(table, columnName))
  }
}

sealed trait SchemaConfigExtractionResult
case class Valid(schemaConfig: SchemaConfig) extends SchemaConfigExtractionResult
case class InvalidInput(errorType: SchemaConfigError) extends SchemaConfigExtractionResult

sealed trait SchemaConfigError
case class InvalidBaseQuery(input: String) extends SchemaConfigError
case class InvalidExtraPrimaryKey(input: String) extends SchemaConfigError
case class InvalidExtraForeignKey(input: String) extends SchemaConfigError
case class InvalidExcludeTable(input: String) extends SchemaConfigError
case class InvalidExcludeColumn(input: String) extends SchemaConfigError
