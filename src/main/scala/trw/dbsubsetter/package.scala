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

  case class Task(table: Table,
                  fk: ForeignKey,
                  values: Seq[AnyRef],
                  fetchChildren: Boolean)

  case class Table(schema: SchemaName,
                   name: TableName)

  case class Column(table: Table,
                    name: ColumnName)

  case class PrimaryKey(table: Table,
                        columns: Vector[Column])

  // The left hand column in each tuple always belongs to the `fromTable`
  // The right hand column in each tuple always belongs to the `toTable`
  case class ForeignKey(fromTable: Table,
                        toTable: Table,
                        columns: Seq[(Column, Column)])
}
