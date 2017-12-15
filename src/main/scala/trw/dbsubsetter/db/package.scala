package trw.dbsubsetter

import java.sql.{Connection, JDBCType}

package object db {
  type SchemaName = String
  type TableName = String
  type ColumnName = String
  type WhereClause = String
  type TypeName = String
  type Row = Array[Any]
  type SqlQuery = String
  type SqlTemplates = Map[(ForeignKey, Table), SqlQuery]

  case class SchemaInfo(tablesByName: Map[(SchemaName, TableName), Table],
                        colsByTableOrdered: Map[Table, Vector[Column]],
                        pkOrdinalsByTable: Map[Table, Vector[Int]],
                        fksOrdered: Array[ForeignKey],
                        fksFromTable: Map[Table, Vector[ForeignKey]],
                        fksToTable: Map[Table, Vector[ForeignKey]])

  case class Table(schema: SchemaName, name: TableName, hasSqlServerAutoIncrement: Boolean, storePks: Boolean)

  case class Column(table: Table, name: ColumnName, ordinalPosition: Int, jdbcType: JDBCType, typeName: String)

  case class ForeignKey(fromCols: Vector[Column], toCols: Vector[Column], pointsToPk: Boolean, i: Short) {
    val fromTable: Table = fromCols.head.table
    val toTable: Table = toCols.head.table

    val isSingleCol: Boolean = fromCols.size == 1
  }

  implicit class VendorAwareJdbcConnection(conn: Connection) {
    private val vendor: String = conn.getMetaData.getDatabaseProductName

    def isMysql: Boolean = vendor == "MySQL"

    def isMsSqlServer: Boolean = vendor == "Microsoft SQL Server"
  }
}
