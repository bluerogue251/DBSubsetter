package trw.dbsubsetter.config

import trw.dbsubsetter.db.{Schema, Table}

import scala.util.matching.Regex

/**
  * Extract command line supplied config values which are simple strings into
  * richer types. If invalid input prevents this, surface this as a validation error.
  */
object ConfigExtractor {
  private val table: String = """([^.\s]*)\.([^.\s]+)"""
  private val columnSet: String = """([^.\s]+)\.([^.\s]+)\((.+)\)"""
  private val separator: String = """\s+:::\s+"""

  def extractConfig(args: CommandLineArgs): ExtractionResult = {
    val schemas = args.schemas.map(Schema)

    val baseQueryRegex = regex(table + separator + """(.+)""" + separator + """(includeChildren|excludeChildren)""")
    val baseQueries =
      args.baseQueries
        .map {
          case baseQueryRegex(schemaName, tableName, whereClause, includeChildren) =>
            val table = normalizeTable(schemaName, tableName)
            ConfigBaseQuery(table, whereClause.trim, includeChildren == "includeChildren")
          case baseQueryString =>
            return Invalid(InvalidBaseQuery(baseQueryString))
        }

    val foreignKeyRegex = regex(columnSet + separator + columnSet)
    val extraForeignKeys =
      args.extraForeignKeys
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
            return Invalid(InvalidExtraForeignKey(foreignKeyString))
        }

    val primaryKeyRegex = regex(columnSet)
    val extraPrimaryKeys =
      args.extraPrimaryKeys
        .map {
          case primaryKeyRegex(schemaName, tableName, cols) =>
            val table = normalizeTable(schemaName, tableName)
            val columns = normalizeColumns(table, cols)
            ConfigPrimaryKey(table, columns)
          case primaryKeyString =>
            return Invalid(InvalidExtraPrimaryKey(primaryKeyString))
        }

    val tableRegex = regex(table)
    val excludeTables =
      args.excludeTables
        .map {
          case tableRegex(schemaName, tableName) =>
            normalizeTable(schemaName, tableName)
          case excludeTableString =>
            return Invalid(InvalidExcludeTable(excludeTableString))
        }

    val columnRegex = regex(columnSet)
    val excludeColumns =
      args.excludeColumns
        .flatMap {
          case columnRegex(schemaName, tableName, cols) =>
            val table = normalizeTable(schemaName, tableName)
            normalizeColumns(table, cols).toSet
          case excludeColumnsString =>
            return Invalid(InvalidExcludeTable(excludeColumnsString))
        }

    Valid(
      SchemaConfig(
        schemas = schemas,
        baseQueries = baseQueries,
        extraForeignKeys = extraForeignKeys,
        extraPrimaryKeys = extraPrimaryKeys,
        excludeTables = excludeTables,
        excludeColumns = excludeColumns
      ),
      Config(
        originDbConnectionString = args.originDbConnectionString,
        targetDbConnectionString = args.targetDbConnectionString,
        keyCalculationDbConnectionCount = args.keyCalculationDbConnectionCount,
        dataCopyDbConnectionCount = args.dataCopyDbConnectionCount,
        tempfileStorageDirectoryOverride = args.tempfileStorageDirectoryOverride
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

  private[this] def regex(regexString: String): Regex = {
    ("""^\s*""" + regexString + """\s*$""").r
  }
}

sealed trait ExtractionResult
case class Valid(schemaConfig: SchemaConfig, config: Config) extends ExtractionResult
case class Invalid(invalidInputType: InvalidInputType) extends ExtractionResult

sealed trait InvalidInputType
case class InvalidBaseQuery(input: String) extends InvalidInputType
case class InvalidExtraPrimaryKey(input: String) extends InvalidInputType
case class InvalidExtraForeignKey(input: String) extends InvalidInputType
case class InvalidExcludeTable(input: String) extends InvalidInputType
case class InvalidExcludeColumns(input: String) extends InvalidInputType
