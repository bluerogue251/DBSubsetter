package trw.dbsubsetter

import java.sql.{DriverManager, JDBCType}

import scala.collection.mutable.ArrayBuffer

object DbSubsetter extends App {
  val schemas = Seq("public", "audit")
  val connectionString = "jdbc:postgresql://localhost:5450/db_subsetter_origin?user=postgres"
  val startingSchema = "public"
  val startingTable = "students"
  val startingWhereClause = "random() < 0.00001"

  val dbConnection = DriverManager.getConnection(connectionString)
  val ddl = dbConnection.getMetaData

  type SchemaName = String
  type TableName = String
  type ColumnName = String

  val tablesMutable = ArrayBuffer.empty[Table]
  val columnsMutable = ArrayBuffer.empty[Column]
  val primaryKeysMutable = ArrayBuffer.empty[PartialPrimaryKey]
  val foreignKeysMutable = ArrayBuffer.empty[PartialForeignKey]

  schemas.foreach { schema =>
    // Args: catalog, schemaPattern, tableNamePattern, types
    val tablesJdbcResultSet = ddl.getTables(null, schema, "%", Array("TABLE"))
    while (tablesJdbcResultSet.next()) {
      tablesMutable += Table(
        tablesJdbcResultSet.getString("TABLE_SCHEM"),
        tablesJdbcResultSet.getString("TABLE_NAME")
      )
    }

    val columnsJdbcResultSet = ddl.getColumns(null, schema, null, null)
    while (columnsJdbcResultSet.next()) {
      columnsMutable += Column(
        columnsJdbcResultSet.getString("TABLE_SCHEM"),
        columnsJdbcResultSet.getString("TABLE_NAME"),
        columnsJdbcResultSet.getString("COLUMN_NAME"),
        JDBCType.valueOf(columnsJdbcResultSet.getInt("DATA_TYPE")),
        columnsJdbcResultSet.getBoolean("NULLABLE")
      )
    }

    // Args: catalog, schema, table
    val primaryKeysJdbcResultSet = ddl.getPrimaryKeys(null, schema, null)
    while (primaryKeysJdbcResultSet.next()) {
      primaryKeysMutable += PartialPrimaryKey(
        primaryKeysJdbcResultSet.getString("TABLE_SCHEM"),
        primaryKeysJdbcResultSet.getString("TABLE_NAME"),
        primaryKeysJdbcResultSet.getString("COLUMN_NAME")
      )
    }

    // Args: catalog, schema, table
    val foreignKeysJdbcResultSet = ddl.getExportedKeys(null, schema, null)
    while (foreignKeysJdbcResultSet.next()) {
      foreignKeysMutable += PartialForeignKey(
        foreignKeysJdbcResultSet.getString("FKTABLE_SCHEM"),
        foreignKeysJdbcResultSet.getString("FKTABLE_NAME"),
        foreignKeysJdbcResultSet.getString("FKCOLUMN_NAME"),
        foreignKeysJdbcResultSet.getString("PKTABLE_SCHEM"),
        foreignKeysJdbcResultSet.getString("PKTABLE_NAME"),
        foreignKeysJdbcResultSet.getString("PKCOLUMN_NAME")
      )
    }
  }

  val colsByTable: Map[(SchemaName, TableName), Map[ColumnName, Column]] = {
    columnsMutable
      .groupBy(c => (c.schema, c.table))
      .map { case ((schema, table), columns) => (schema, table) -> columns.map(c => c.name -> c).toMap }
  }

  val pksOfTable: Map[(SchemaName, TableName), Vector[Column]] = {
    primaryKeysMutable
      .groupBy(pk => (pk.schema, pk.table))
      .map { case ((schema, table), partialPks) =>
        (schema, table) -> partialPks.map(ppk => colsByTable((schema, table))(ppk.column)).toVector
      }
  }

  val fksFromTable: Map[(SchemaName, TableName), Vector[(Column, Column)]] = {
    foreignKeysMutable
      .groupBy(fk => (fk.fromSchema, fk.fromTable))
      .map { case (k, partialFks) =>
        k -> partialFks.map { pfk =>
          (colsByTable(pfk.fromSchema, pfk.fromTable)(pfk.fromColumn), colsByTable(pfk.toSchema, pfk.toTable)(pfk.toColumn))
        }.toVector
      }
  }

  val fksPointingToTable: Map[(SchemaName, TableName), Vector[(Column, Column)]] = {
    foreignKeysMutable
      .groupBy(fk => (fk.toSchema, fk.toTable))
      .map { case (k, partialFks) =>
        k -> partialFks.map { pfk =>
          (colsByTable(pfk.fromSchema, pfk.fromTable)(pfk.fromColumn), colsByTable(pfk.toSchema, pfk.toTable)(pfk.toColumn))
        }.toVector
      }
  }

  // There must be exactly one primary key
  val initialPkCols = pksOfTable((startingSchema, startingTable))
  val initialQuery = s"select ${initialPkCols.map(_.name).mkString(",")} from $startingSchema.$startingTable where $startingWhereClause"
  val initialParentsToFind = fksFromTable((startingSchema, startingTable))
  val initialChildrenToFind = fksPointingToTable((startingSchema, startingTable))
  val columnsWeCareAbout = initialPkCols

  val statement = dbConnection.createStatement()
  val resultSet = statement.executeQuery(initialQuery)
  while (resultSet.next()) {
    println(resultSet.getString("student_id"))
  }
}

case class Table(schema: String,
                 name: String)

case class Column(schema: String,
                  table: String,
                  name: String,
                  jdbcType: JDBCType,
                  nullable: Boolean)

case class PartialPrimaryKey(schema: String,
                             table: String,
                             column: String)

case class PrimaryKey(tableSchema: String,
                      tableName: String,
                      columns: Seq[Column])

case class PartialForeignKey(fromSchema: String,
                             fromTable: String,
                             fromColumn: String,
                             toSchema: String,
                             toTable: String,
                             toColumn: String)
