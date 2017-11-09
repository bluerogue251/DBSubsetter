package trw.dbsubsetter.db

import java.sql.Connection

import trw.dbsubsetter._

import scala.collection.mutable.ArrayBuffer

object SchemaInfoRetrieval {
  def getSchemaInfo(conn: Connection, schemas: Seq[String]): SchemaInfo = {
    val ddl = conn.getMetaData

    val tablesQueryResult = ArrayBuffer.empty[Table]
    val columnsQueryResult = ArrayBuffer.empty[ColumnQueryRow]
    val primaryKeysQueryResult = ArrayBuffer.empty[PrimaryKeyQueryRow]
    val foreignKeysQueryResult = ArrayBuffer.empty[ForeignKeyQueryRow]

    schemas.foreach { schema =>
      // Args: catalog, schemaPattern, tableNamePattern, types
      val tablesJdbcResultSet = ddl.getTables(null, schema, "%", Array("TABLE"))
      while (tablesJdbcResultSet.next()) {
        tablesQueryResult += Table(
          tablesJdbcResultSet.getString("TABLE_SCHEM"),
          tablesJdbcResultSet.getString("TABLE_NAME")
        )
      }

      val columnsJdbcResultSet = ddl.getColumns(null, schema, null, null)
      while (columnsJdbcResultSet.next()) {
        columnsQueryResult += ColumnQueryRow(
          columnsJdbcResultSet.getString("TABLE_SCHEM"),
          columnsJdbcResultSet.getString("TABLE_NAME"),
          columnsJdbcResultSet.getString("COLUMN_NAME")
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
      (table.schema, table.name) -> table
    }.toMap

    val colsByTable: Map[Table, Map[ColumnName, Column]] = {
      columnsQueryResult
        .groupBy(c => tablesByName(c.schema, c.table))
        .map { case (table, partialColumns) =>
          table -> partialColumns.map(pc => pc.name -> Column(table, pc.name)).toMap
        }
    }

    val pksByTable: Map[Table, PrimaryKey] = {
      primaryKeysQueryResult
        .groupBy(pk => tablesByName(pk.schema, pk.table))
        .map { case (table, partialPks) =>
          table -> PrimaryKey(
            table,
            partialPks.map(ppk => colsByTable(table)(ppk.column)).toVector
          )
        }
    }

    val foreignKeys: Set[ForeignKey] = {
      foreignKeysQueryResult
        .groupBy(fkm => (fkm.fromSchema, fkm.fromTable, fkm.toSchema, fkm.toTable))
        .map { case ((fromSchemaName, fromTableName, toSchemaName, toTableName), partialForeignKeys) =>
          val fromTable = tablesByName(fromSchemaName, fromTableName)
          val fromCols = partialForeignKeys.map { pfk => colsByTable(fromTable)(pfk.fromColumn) }.toVector
          val toTable = tablesByName(toSchemaName, toTableName)
          val toCols = partialForeignKeys.map { pfk => colsByTable(toTable)(pfk.toColumn) }.toVector
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
      pksByTable,
      foreignKeys,
      fksFromTable,
      fksToTable
    )
  }
}

case class SchemaInfo(tablesByName: Map[(SchemaName, TableName), Table],
                      pksByTable: Map[Table, PrimaryKey],
                      fks: Set[ForeignKey],
                      fksFromTable: Map[Table, Set[ForeignKey]],
                      fksToTable: Map[Table, Set[ForeignKey]])

case class ColumnQueryRow(schema: SchemaName,
                          table: TableName,
                          name: ColumnName)

case class PrimaryKeyQueryRow(schema: SchemaName,
                              table: TableName,
                              column: ColumnName)

case class ForeignKeyQueryRow(fromSchema: SchemaName,
                              fromTable: TableName,
                              fromColumn: ColumnName,
                              toSchema: SchemaName,
                              toTable: TableName,
                              toColumn: ColumnName)
