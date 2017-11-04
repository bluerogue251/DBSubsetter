package trw.dbsubsetter

import java.sql.Connection

import scala.collection.mutable.ArrayBuffer

object SchemaInfoRetrieval {
  def getSchemaInfo(conn: Connection, schemas: Set[String]): SchemaInfo = {
    val ddl = conn.getMetaData

    val tablesMutable = ArrayBuffer.empty[Table]
    val columnsMutable = ArrayBuffer.empty[PartialColumn]
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
        columnsMutable += PartialColumn(
          columnsJdbcResultSet.getString("TABLE_SCHEM"),
          columnsJdbcResultSet.getString("TABLE_NAME"),
          columnsJdbcResultSet.getString("COLUMN_NAME")
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
    val tablesByName = tables.map { table =>
      (table.schema, table.name) -> table
    }.toMap

    val colsByTable: Map[Table, Map[ColumnName, Column]] = {
      columnsMutable
        .groupBy(c => tablesByName(c.schema, c.table))
        .map { case (table, partialColumns) =>
          table -> partialColumns.map(pc => pc.name -> Column(table, pc.name)).toMap
        }
    }

    val pksByTable: Map[Table, PrimaryKey] = {
      primaryKeysMutable
        .groupBy(pk => tablesByName(pk.schema, pk.table))
        .map { case (table, partialPks) =>
          table -> PrimaryKey(
            table,
            partialPks.map(ppk => colsByTable(table)(ppk.column)).toVector
          )
        }
    }

    val foreignKeys: Set[ForeignKey] = {
      foreignKeysMutable
        .groupBy(fkm => (fkm.fromSchema, fkm.fromTable, fkm.toSchema, fkm.toTable))
        .map { case ((fromSchemaName, fromTableName, toSchemaName, toTableName), partialForeignKeys) =>
          val fromTable = tablesByName(fromSchemaName, fromTableName)
          val toTable = tablesByName(toSchemaName, toTableName)
          ForeignKey(
            fromTable,
            toTable,
            partialForeignKeys.map { pfk => (colsByTable(fromTable)(pfk.fromColumn), colsByTable(toTable)(pfk.toColumn)) }.toVector
          )
        }
        .toSet
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

case class PartialColumn(schema: SchemaName,
                         table: TableName,
                         name: ColumnName)

case class PartialPrimaryKey(schema: SchemaName,
                             table: TableName,
                             column: ColumnName)

case class PartialForeignKey(fromSchema: SchemaName,
                             fromTable: TableName,
                             fromColumn: ColumnName,
                             toSchema: SchemaName,
                             toTable: TableName,
                             toColumn: ColumnName)