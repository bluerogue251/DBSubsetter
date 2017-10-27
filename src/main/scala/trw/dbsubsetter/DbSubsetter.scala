package trw.dbsubsetter

import java.sql.DriverManager

import scala.collection.mutable.ArrayBuffer

object DbSubsetter extends App {
  // TODO: Pull out into configuration
  val schemas = Seq("public", "audit")
  val connectionString = "jdbc:postgresql://localhost:5450/db_subsetter_origin?user=postgres"


  val dbConnection = DriverManager.getConnection(connectionString)
  val ddl = dbConnection.getMetaData

  val tablesMutable = ArrayBuffer.empty[DbTable]
  schemas.foreach { schema =>
    // Arguments are: catalog, schemaPattern, tableNamePattern, types
    val tablesJdbcResultSet = ddl.getTables(null, schema, "%", Array("TABLE"))
    while (tablesJdbcResultSet.next()) {
      tablesMutable += DbTable(tablesJdbcResultSet.getString("TABLE_SCHEM"), tablesJdbcResultSet.getString("TABLE_NAME"))
    }
  }
  val tables = tablesMutable.toVector
  println(tables)

  val primaryKeysMutable = ArrayBuffer.empty[DbPrimaryKey]
  schemas.foreach { schema =>
    // Arguments are: catalog, schema, table
    val primaryKeysJdbcResultSet = ddl.getPrimaryKeys(null, schema, null)
    while (primaryKeysJdbcResultSet.next()) {
      primaryKeysMutable += DbPrimaryKey(
        primaryKeysJdbcResultSet.getString("TABLE_SCHEM"),
        primaryKeysJdbcResultSet.getString("TABLE_NAME"),
        primaryKeysJdbcResultSet.getString("COLUMN_NAME")
      )
    }
  }
  val primaryKeys = primaryKeysMutable.toVector
  println(primaryKeys)
}

case class DbTable(schema: String, name: String)

case class DbPrimaryKey(tableSchema: String, tableName: String, columnName: String)