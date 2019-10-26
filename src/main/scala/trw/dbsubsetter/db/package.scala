package trw.dbsubsetter

import java.sql.Connection

import trw.dbsubsetter.db.ColumnTypes.ColumnType

package object db {
  type SchemaName = String
  type TableName = String
  type ColumnName = String
  type WhereClause = String
  type TypeName = String
  type Row = Array[Any]
  type SqlQuery = String
  type SqlTemplates = Map[(ForeignKey, Table), SqlQuery]

  class SchemaInfo(
    val tablesByName: Map[(SchemaName, TableName), Table],
    val colsByTableOrdered: Map[Table, Vector[Column]],
    val pksByTableOrdered: Map[Table, Vector[Column]],
    val fksOrdered: Array[ForeignKey],
    val fksFromTable: Map[Table, Vector[ForeignKey]],
    val fksToTable: Map[Table, Vector[ForeignKey]],
    val dbVendor: DbVendor
  )

  class Table(
    val schema: SchemaName,
    val name: TableName,
    val hasSqlServerAutoIncrement: Boolean
  )

  class Column(
    val table: Table,
    val name: ColumnName,
    val ordinalPosition: Int,
    val dataType: ColumnType
  )

  class ForeignKey(
    val fromCols: Vector[Column],
    val toCols: Vector[Column],
    val pointsToPk: Boolean,
    var i: Short
  ) {
    val fromTable: Table = fromCols.head.table
    val toTable: Table = toCols.head.table
    val isSingleCol: Boolean = fromCols.size == 1
    // TODO Refactor to remove mutability
    def setIndex(index: Short): Unit = {
      i = index
    }
  }

  implicit class VendorAwareJdbcConnection(private val conn: Connection) {
    private val vendorName: String = conn.getMetaData.getDatabaseProductName

    val dbVendor: DbVendor = vendorName match {
      case "Microsoft SQL Server" => DbVendor.MicrosoftSQLServer
      case "MySQL" => DbVendor.MySQL
      case "PostgreSQL" => DbVendor.PostgreSQL
    }

    def isMysql: Boolean = dbVendor == DbVendor.MySQL

    def isMsSqlServer: Boolean = dbVendor == DbVendor.MicrosoftSQLServer
  }
}
