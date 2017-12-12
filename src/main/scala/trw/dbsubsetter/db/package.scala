package trw.dbsubsetter

import java.sql.{Connection, JDBCType}

import scala.collection.mutable

package object db {
  type SchemaName = String
  type TableName = String
  type ColumnName = String
  type FullyQualifiedTableName = String
  type JoinClause = String
  type WhereClause = String
  type PrimaryKeyStore = Map[Table, mutable.HashSet[Vector[AnyRef]]]
  type Row = Array[AnyRef]
  type SqlQuery = String
  type SqlTemplates = Map[(ForeignKey, Table), SqlQuery]

  case class SchemaInfo(tablesByName: Map[(SchemaName, TableName), Table],
                        colsByTableOrdered: Map[Table, Vector[Column]],
                        pkOrdinalsByTable: Map[Table, Vector[Int]],
                        fks: Set[ForeignKey],
                        fksFromTable: Map[Table, Set[ForeignKey]],
                        fksToTable: Map[Table, Set[ForeignKey]])

  case class Table(schema: SchemaName, name: TableName, hasSqlServerAutoIncrement: Boolean)

  case class Column(table: Table, name: ColumnName, ordinalPosition: Int, jdbcType: JDBCType)

  case class ForeignKey(fromCols: Vector[Column], toCols: Vector[Column], pointsToPk: Boolean) {
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
