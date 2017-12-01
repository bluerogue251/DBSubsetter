package trw.dbsubsetter.db

import java.sql.DriverManager

import trw.dbsubsetter.config.Config

import scala.collection.mutable.ArrayBuffer

object SchemaInfoRetrieval {
  def getSchemaInfo(config: Config): SchemaInfo = {
    val conn = DriverManager.getConnection(config.originDbConnectionString)
    conn.setReadOnly(true)
    val isMysql = conn.getMetaData.getDatabaseProductName == "MySQL"
    val catalog = conn.getCatalog
    val ddl = conn.getMetaData

    val tablesQueryResult = ArrayBuffer.empty[Table]
    val columnsQueryResult = ArrayBuffer.empty[ColumnQueryRow]
    val primaryKeysQueryResult = ArrayBuffer.empty[PrimaryKeyQueryRow]
    val foreignKeysQueryResult = ArrayBuffer.empty[ForeignKeyQueryRow]

    config.schemas.foreach { schema =>
      // Args: catalog, schemaPattern, tableNamePattern, types
      val tablesJdbcResultSet = {
        if (isMysql) ddl.getTables(schema, null, "%", Array("TABLE"))
        else ddl.getTables(catalog, schema, "%", Array("TABLE"))
      }
      while (tablesJdbcResultSet.next()) {
        tablesQueryResult += Table(
          schema,
          tablesJdbcResultSet.getString("TABLE_NAME")
        )
      }
    }

    tablesQueryResult.foreach { table =>
      val colsJdbcResultSet = {
        if (isMysql) ddl.getColumns(table.schema, null, table.name, "%")
        else ddl.getColumns(catalog, table.schema, table.name, "%")
      }
      while (colsJdbcResultSet.next()) {
        val columnName = colsJdbcResultSet.getString("COLUMN_NAME")

        if (!config.excludeColumns((table.schema, table.name)).contains(columnName)) {
          columnsQueryResult += ColumnQueryRow(table.schema, table.name, columnName)
        }
      }
    }

    // Args: catalog, schema, table
    tablesQueryResult.foreach { table =>
      val pksJdbcResultSet = {
        if (isMysql) ddl.getPrimaryKeys(table.schema, null, table.name)
        else ddl.getPrimaryKeys(catalog, table.schema, table.name)
      }
      while (pksJdbcResultSet.next()) {
        primaryKeysQueryResult += PrimaryKeyQueryRow(
          table.schema,
          table.name,
          pksJdbcResultSet.getString("COLUMN_NAME")
        )
      }
    }

    tablesQueryResult.foreach { table =>
      // Args: catalog, schema, table
      val foreignKeysJdbcResultSet = {
        if (isMysql) ddl.getImportedKeys(table.schema, null, table.name)
        else ddl.getImportedKeys(catalog, table.schema, table.name)
      }
      while (foreignKeysJdbcResultSet.next()) {
        foreignKeysQueryResult += ForeignKeyQueryRow(
          if (isMysql) foreignKeysJdbcResultSet.getString("FKTABLE_CAT") else foreignKeysJdbcResultSet.getString("FKTABLE_SCHEM"),
          foreignKeysJdbcResultSet.getString("FKTABLE_NAME"),
          foreignKeysJdbcResultSet.getString("FKCOLUMN_NAME"),
          if (isMysql) foreignKeysJdbcResultSet.getString("PKTABLE_CAT") else foreignKeysJdbcResultSet.getString("PKTABLE_SCHEM"),
          foreignKeysJdbcResultSet.getString("PKTABLE_NAME"),
          foreignKeysJdbcResultSet.getString("PKCOLUMN_NAME")
        )
      }
    }

    conn.close()

    config.cmdLinePrimaryKeys.foreach { clpk =>
      clpk.columns.foreach(c => primaryKeysQueryResult += PrimaryKeyQueryRow(clpk.schema, clpk.table, c))
    }

    val tables = tablesQueryResult.toVector
    val tablesByName = tables.map(t => (t.schema, t.name) -> t).toMap

    val colsByTableAndName: Map[Table, Map[ColumnName, Column]] = {
      columnsQueryResult
        .groupBy(c => tablesByName(c.schema, c.table))
        .map { case (table, partialColumns) =>
          table -> partialColumns.zipWithIndex.map { case (pc, i) => pc.name -> Column(table, pc.name, i) }.toMap
        }
    }
    val colByTableOrdered: Map[Table, Vector[Column]] = {
      colsByTableAndName.map { case (table, map) => table -> map.values.toVector.sortBy(_.ordinalPosition) }
    }

    val pkColumnOrdinalsByTable: Map[Table, Vector[Int]] = {
      primaryKeysQueryResult
        .groupBy(pk => tablesByName(pk.schema, pk.table))
        .map { case (table, partialPks) =>
          table -> partialPks.map(ppk => colsByTableAndName(table)(ppk.column)).toVector.map(_.ordinalPosition).sorted
        }
    }

    val foreignKeys: Set[ForeignKey] = {
      val userSuppliedPartialFks: Seq[ForeignKeyQueryRow] = config.cmdLineForeignKeys.flatMap { cfk =>
        cfk.fromColumns.zip(cfk.toColumns).map { case (fromCol, toCol) =>
          ForeignKeyQueryRow(cfk.fromSchema, cfk.fromTable, fromCol, cfk.toSchema, cfk.toTable, toCol)
        }
      }
      val allPartialFKs = userSuppliedPartialFks ++ foreignKeysQueryResult

      allPartialFKs.groupBy(fkm => (fkm.fromSchema, fkm.fromTable, fkm.toSchema, fkm.toTable))
        .map { case ((fromSchemaName, fromTableName, toSchemaName, toTableName), partialForeignKeys) =>
          val fromTable = tablesByName(fromSchemaName, fromTableName)
          val fromCols = partialForeignKeys.map { pfk => colsByTableAndName(fromTable)(pfk.fromColumn) }.toVector
          val toTable = tablesByName(toSchemaName, toTableName)
          val toCols = partialForeignKeys.map { pfk => colsByTableAndName(toTable)(pfk.toColumn) }.toVector
          val pointsToPk = {
            val pkOpt = pkColumnOrdinalsByTable.get(toTable)
            pkOpt.fold(false)(pkColOrdinals => pkColOrdinals == toCols.map(_.ordinalPosition))
          }

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
      colByTableOrdered,
      pkColumnOrdinalsByTable,
      foreignKeys,
      fksFromTable,
      fksToTable
    )
  }

  private[this] case class ColumnQueryRow(schema: SchemaName,
                                          table: TableName,
                                          name: ColumnName)

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