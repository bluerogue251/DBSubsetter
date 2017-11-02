package trw

import java.sql.JDBCType

package object dbsubsetter {
  type SchemaName = String
  type TableName = String
  type ColumnName = String
  type WhereClause = String

  case class Table(schema: SchemaName,
                   name: TableName)

  case class Column(schema: SchemaName,
                    table: TableName,
                    name: ColumnName,
                    jdbcType: JDBCType,
                    nullable: Boolean)

  case class PartialPrimaryKey(schema: SchemaName,
                               table: TableName,
                               column: ColumnName)

  case class PrimaryKey(tableSchema: SchemaName,
                        tableName: TableName,
                        columns: Seq[Column])

  case class PartialForeignKey(fromSchema: SchemaName,
                               fromTable: TableName,
                               fromColumn: ColumnName,
                               toSchema: SchemaName,
                               toTable: TableName,
                               toColumn: ColumnName)

  // The left hand column is the `fromColumn`, the right hand column is the `toColumn`
  case class ForeignKey(fromSchema: SchemaName,
                        fromTable: TableName,
                        toSchema: SchemaName,
                        toTable: TableName,
                        columns: Set[(Column, Column)])

}
