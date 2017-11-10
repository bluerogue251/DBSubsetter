package trw.dbsubsetter

import scala.collection.mutable

package object db {
  type SchemaName = String
  type TableName = String
  type ColumnName = String
  type FullyQualifiedTableName = String
  type WhereClause = String
  type PrimaryKeyStore = Map[Table, mutable.HashSet[Vector[AnyRef]]]
  type Row = Map[Column, AnyRef]
  type SqlQuery = String
  type SqlTemplates = Map[(ForeignKey, Table, Boolean), (SqlQuery, Seq[Column])]

  case class SchemaInfo(tablesByName: Map[(SchemaName, TableName), Table],
                        pksByTable: Map[Table, PrimaryKey],
                        fks: Set[ForeignKey],
                        fksFromTable: Map[Table, Set[ForeignKey]],
                        fksToTable: Map[Table, Set[ForeignKey]])

  case class Table(schema: SchemaName, name: TableName) {
    val fullyQualifiedName: String = s""""$schema"."$name""""
  }

  case class Column(table: Table, name: ColumnName) {
    val fullyQualifiedName: String = s"""${table.fullyQualifiedName}."$name""""
  }

  case class PrimaryKey(table: Table, columns: Vector[Column])

  // The left hand column in each tuple always belongs to the `fromTable`
  // The right hand column in each tuple always belongs to the `toTable`
  case class ForeignKey(fromCols: Vector[Column], toCols: Vector[Column], pointsToPk: Boolean) {
    val fromTable: Table = fromCols.head.table
    val toTable: Table = toCols.head.table
  }
}
