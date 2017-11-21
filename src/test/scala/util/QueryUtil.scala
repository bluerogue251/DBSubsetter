package util

import java.sql.Connection

import trw.dbsubsetter.db.{ColumnName, SchemaName, TableName}

trait QueryUtil {
  def targetConn: Connection

  def countTable(schema: SchemaName, table: TableName): Long = {
    val resultSet = targetConn.createStatement().executeQuery(s"""select count(*) from "$schema"."$table"""")
    resultSet.next()
    resultSet.getLong(1)
  }

  def sumColumn(schema: SchemaName, table: TableName, column: ColumnName): Long = {
    val resultSet = targetConn.createStatement().executeQuery(s"""select sum("$column") from "$schema"."$table"""")
    resultSet.next()
    resultSet.getLong(1)
  }
}
