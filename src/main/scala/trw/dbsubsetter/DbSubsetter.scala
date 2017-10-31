package trw.dbsubsetter

import java.sql.{DriverManager, JDBCType}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object DbSubsetter extends App {
  val schemas = Seq("public", "audit")
  val connectionString = "jdbc:postgresql://localhost:5450/db_subsetter_origin?user=postgres"
  val startingSchema = "public"
  val startingTable = "students"
  val startingWhereClause = "random() < 0.00001"

  val dbConnection = DriverManager.getConnection(connectionString)
  val ddl = dbConnection.getMetaData

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

  val tables = tablesMutable.toVector

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

  val foreignKeys: Set[ForeignKey] = {
    foreignKeysMutable
      .groupBy(fkm => (fkm.fromSchema, fkm.fromTable, fkm.toSchema, fkm.toTable))
      .map { case ((fromSchema, fromTable, toSchema, toTable), partialForeignKeys) =>
        ForeignKey(
          fromSchema,
          fromTable,
          toSchema,
          toTable,
          partialForeignKeys.map { pfk => (colsByTable((fromSchema, fromTable))(pfk.fromColumn), colsByTable((toSchema, toTable))(pfk.toColumn)) }.toSet
        )
      }
      .toSet
  }

  val fksFromTable: Map[(SchemaName, TableName), Set[ForeignKey]] = {
    foreignKeys.groupBy(fk => (fk.fromSchema, fk.fromTable)).withDefaultValue(Set.empty)
  }

  val fksPointingToTable: Map[(SchemaName, TableName), Set[ForeignKey]] = {
    foreignKeys.groupBy(fk => (fk.toSchema, fk.toTable)).withDefaultValue(Set.empty)
  }

  // Queue of items still to be processed
  val processingQueue = mutable.Queue.empty[(SchemaName, TableName, String, Boolean)]

  // In-memory storage for primary key values
  val primaryKeyStore: Map[(SchemaName, TableName), mutable.HashSet[Vector[AnyRef]]] = {
    tables.map(table => (table.schema, table.name) -> mutable.HashSet.empty[Vector[AnyRef]]).toMap
  }

  def process(): Unit = {
    while (processingQueue.nonEmpty) {
      val (schema, table, whereClause, fetchChildren) = processingQueue.dequeue()
      // Figure out which columns we need to select out of this table
      // So that we don't select any more data than is absolutely necessary
      val pk = pksOfTable((schema, table))
      val parentFks = fksFromTable((schema, table))
      val childFks = fksPointingToTable((schema, table))
      val columnsToSelect: Seq[Column] = pk ++ parentFks.flatMap(_.columns).map { case (fromCol, _) => fromCol } ++ childFks.flatMap(_.columns).map { case (_, toCol) => toCol }

      // Build and execute the SQL statement to select the data matching the where clause
      val query =
        s"""select ${columnsToSelect.map(_.name).mkString(", ")}
           | from $schema.$table
           | where $whereClause
           | """.stripMargin
      val resultSet = dbConnection.createStatement().executeQuery(query)

      // Put the result in a collection of Maps from column names to values, each element in the collection is a row of the result
      // Could we be more efficient by doing this by index rather than by column name?
      val tmpResult = ArrayBuffer.empty[Map[ColumnName, AnyRef]]
      while (resultSet.next()) {
        tmpResult += columnsToSelect.map(col => col.name -> resultSet.getObject(col.name)).toMap
      }

      // Find out which rows are "new" in the sense of having not yet been processed by us
      // Add the primary key of each of the "new" rows to the primaryKeyStore.
      val newRows = tmpResult.filter { row =>
        primaryKeyStore((schema, table)).add(pk.map(k => row(k.name)))
      }

      // For each "new" row, call `process` method recursively on its parents and children
      newRows.foreach { row =>
        parentFks.foreach { pfk =>
          val whereClause = pfk.columns.flatMap { case (fromCol, toCol) =>
            Option(row(fromCol.name)).map(fromColValue => s"${toCol.name} = '$fromColValue'")
          }.mkString(" and ")
          if (whereClause.nonEmpty) processingQueue.enqueue((pfk.toSchema, pfk.toTable, whereClause, false))
        }

        childFks.foreach { cfk =>
          val whereClause = cfk.columns.flatMap { case (fromCol, toCol) =>
            Option(row(toCol.name)).map(toColValue => s"${fromCol.name} = '$toColValue'")
          }.mkString(" and ")
          if (whereClause.nonEmpty) processingQueue.enqueue((cfk.fromSchema, cfk.fromTable, whereClause, fetchChildren))
        }
      }

      // Print Debug info about what primary keys we have so far
      primaryKeyStore.foreach { case ((schemaName, tableName), hashSet) =>
        println(s"$schemaName.$tableName: ${hashSet.size}")
      }
    }
  }

  processingQueue.enqueue((startingSchema, startingTable, startingWhereClause, true))
  process()
}