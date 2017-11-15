package trw.dbsubsetter.db

import java.sql.DriverManager

import trw.dbsubsetter.config.Config

import scala.collection.mutable.ArrayBuffer

object SchemaInfoRetrieval {
  def getSchemaInfo(config: Config): SchemaInfo = {
    val conn = DriverManager.getConnection(config.originDbConnectionString)
    conn.setReadOnly(true)
    val ddl = conn.getMetaData

    val tablesQueryResult = ArrayBuffer.empty[TableQueryRow]
    val columnsQueryResult = ArrayBuffer.empty[ColumnQueryRow]
    val primaryKeysQueryResult = ArrayBuffer.empty[PrimaryKeyQueryRow]
    val foreignKeysQueryResult = ArrayBuffer.empty[ForeignKeyQueryRow]

    config.schemas.foreach { schema =>
      // Args: catalog, schemaPattern, tableNamePattern, types
      val tablesJdbcResultSet = ddl.getTables(null, schema, "%", Array("TABLE"))
      while (tablesJdbcResultSet.next()) {
        tablesQueryResult += TableQueryRow(
          tablesJdbcResultSet.getString("TABLE_SCHEM"),
          tablesJdbcResultSet.getString("TABLE_NAME")
        )
      }

      val columnsJdbcResultSet = ddl.getColumns(null, schema, null, null)
      while (columnsJdbcResultSet.next()) {
        columnsQueryResult += ColumnQueryRow(
          columnsJdbcResultSet.getString("TABLE_SCHEM"),
          columnsJdbcResultSet.getString("TABLE_NAME"),
          columnsJdbcResultSet.getString("COLUMN_NAME"),
          columnsJdbcResultSet.getInt("ORDINAL_POSITION")
        )
      }

      // Args: catalog, schema, table
      val primaryKeysJdbcResultSet = ddl.getPrimaryKeys(null, schema, null)
      while (primaryKeysJdbcResultSet.next()) {
        primaryKeysQueryResult += PrimaryKeyQueryRow(
          primaryKeysJdbcResultSet.getString("TABLE_SCHEM"),
          primaryKeysJdbcResultSet.getString("TABLE_NAME"),
          primaryKeysJdbcResultSet.getString("COLUMN_NAME")
        )
      }

      // Args: catalog, schema, table
      val foreignKeysJdbcResultSet = ddl.getExportedKeys(null, schema, null)
      while (foreignKeysJdbcResultSet.next()) {
        foreignKeysQueryResult += ForeignKeyQueryRow(
          foreignKeysJdbcResultSet.getString("FKTABLE_SCHEM"),
          foreignKeysJdbcResultSet.getString("FKTABLE_NAME"),
          foreignKeysJdbcResultSet.getString("FKCOLUMN_NAME"),
          foreignKeysJdbcResultSet.getString("PKTABLE_SCHEM"),
          foreignKeysJdbcResultSet.getString("PKTABLE_NAME"),
          foreignKeysJdbcResultSet.getString("PKCOLUMN_NAME")
        )
      }
    }

    val tables = tablesQueryResult.toVector
    val tablesByName = tables.map { table =>
      val pkColumnNames = primaryKeysQueryResult
        .filter(pk => pk.schema == table.schema && pk.table == table.name)
        .map(_.column).toSet
      val pkColumnOrdinals = columnsQueryResult
        .filter(col => pkColumnNames.contains(col.name))
        .map(_.ordinalPosition)
        .sorted
      (table.schema, table.name) -> Table(table.schema, table.name, pkColumnOrdinals)
    }.toMap

    val colsByTableAndName: Map[Table, Map[ColumnName, Column]] = {
      columnsQueryResult
        .groupBy(c => tablesByName(c.schema, c.table))
        .map { case (table, partialColumns) =>
          table -> partialColumns.map(pc => pc.name -> Column(table, pc.name, pc.ordinalPosition)).toMap
        }
    }
    val colNamesByTableOrdered: Map[Table, Vector[ColumnName]] = {
      colsByTableAndName.map { case (table, map) => table -> map.keys.toVector }
    }

    val foreignKeys: Set[ForeignKey] = {
      foreignKeysQueryResult
        .groupBy(fkm => (fkm.fromSchema, fkm.fromTable, fkm.toSchema, fkm.toTable))
        .map { case ((fromSchemaName, fromTableName, toSchemaName, toTableName), partialForeignKeys) =>
          val fromTable = tablesByName(fromSchemaName, fromTableName)
          val fromCols = partialForeignKeys.map { pfk => colsByTableAndName(fromTable)(pfk.fromColumn) }.toVector
          val toTable = tablesByName(toSchemaName, toTableName)
          val toCols = partialForeignKeys.map { pfk => colsByTableAndName(toTable)(pfk.toColumn) }.toVector
          val pointsToPk = pksByTable(toTable).columns == toCols

          ForeignKey(fromCols, toCols, pointsToPk)
        }.toSet
    }

    val fksFromTable: Map[Table, Set[ForeignKey]] = {
      foreignKeys.groupBy(_.fromTable).withDefaultValue(Set.empty)
    }

    val fksToTable: Map[Table, Set[ForeignKey]] = {
      foreignKeys.groupBy(_.toTable).withDefaultValue(Set.empty)
    }

    SchemaInfo(
      tablesByName,
      colNamesByTableOrdered,
      foreignKeys,
      fksFromTable,
      fksToTable
    )
  }

  private[this] case class TableQueryRow(schema: SchemaName,
                                         name: TableName)

  private[this] case class ColumnQueryRow(schema: SchemaName,
                                          table: TableName,
                                          name: ColumnName,
                                          ordinalPosition: Int)

  private[this] case class PrimaryKeyQueryRow(schema: SchemaName,
                                              table: TableName,
                                              column: ColumnName)

  private[this] case class ForeignKeyQueryRow(fromSchema: SchemaName,
                                              fromTable: TableName,
                                              fromColumn: ColumnName,
                                              toSchema: SchemaName,
                                              toTable: TableName,
                                              toColumn: ColumnName)

}


