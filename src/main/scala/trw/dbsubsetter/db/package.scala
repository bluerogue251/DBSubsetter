package trw.dbsubsetter

import scala.collection.mutable

package object db {
  type SchemaName = String
  type TableName = String
  type ColumnName = String
  type FullyQualifiedTableName = String
  type WhereClause = String
  type PrimaryKeyStore = Map[Table, mutable.HashSet[Vector[AnyRef]]]
  type Row = Array[AnyRef]
  type SqlQuery = String
  type SqlTemplates = Map[(ForeignKey, Table), SqlQuery]

  case class SchemaInfo(tablesByName: Map[(SchemaName, TableName), Table],
                        colsByTable: Map[Table, Vector[Column]],
                        pkColsByTable: Map[Table, Vector[Column]],
                        fks: Set[ForeignKey],
                        fksFromTable: Map[Table, Set[ForeignKey]],
                        fksToTable: Map[Table, Set[ForeignKey]])

  case class Table(schema: SchemaName, name: TableName) {
    val fullyQualifiedName: String = s""""$schema"."$name""""
  }

  case class Column(table: Table, name: ColumnName, ordinalPosition: Int) {
    val quotedName: String = s""""$name""""
    val fullyQualifiedName: String = s"""${table.fullyQualifiedName}.$quotedName"""
  }

  // The left hand column in each tuple always belongs to the `fromTable`
  // The right hand column in each tuple always belongs to the `toTable`
  case class ForeignKey(fromCols: Vector[Column], toCols: Vector[Column], pointsToPk: Boolean) {
    val fromTable: Table = fromCols.head.table
    val toTable: Table = toCols.head.table
  }
}
