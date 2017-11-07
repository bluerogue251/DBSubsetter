package trw

import scala.collection.mutable

package object dbsubsetter {
  type SchemaName = String
  type TableName = String
  type ColumnName = String
  type FullyQualifiedTableName = String
  type WhereClause = String
  type PrimaryKeyStore = Map[Table, mutable.HashSet[Vector[AnyRef]]]
  type Row = Map[Column, AnyRef]
  type SqlQuery = String

  case class Task(table: Table, fk: ForeignKey, values: Vector[AnyRef], fetchChildren: Boolean)

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
    require(fromCols.nonEmpty)
    require(fromCols.length == toCols.length)
    require(fromCols.map(_.table).distinct.length == 1)
    require(toCols.map(_.table).distinct.length == 1)

    val fromTable = fromCols.head.table
    val toTable = toCols.head.table
  }
}
