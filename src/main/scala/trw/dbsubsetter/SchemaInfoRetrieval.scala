package trw.dbsubsetter

import java.sql.{Connection, JDBCType}

import scala.collection.mutable.ArrayBuffer

object DbSchemaInfoRetrieval {
  def getSchemaInfo(conn: Connection, schemas: Set[String]): DbSchemaInfo = {
    val ddl = conn.getMetaData

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

    val pksByTable: Map[(SchemaName, TableName), Vector[Column]] = {
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

    val fksToTable: Map[(SchemaName, TableName), Set[ForeignKey]] = {
      foreignKeys.groupBy(fk => (fk.toSchema, fk.toTable)).withDefaultValue(Set.empty)
    }

    DbSchemaInfo(
      tables,
      colsByTable,
      pksByTable,
      foreignKeys,
      fksFromTable,
      fksToTable
    )
  }
}

case class DbSchemaInfo(tables: Vector[Table],
                        colsByTable: Map[(SchemaName, TableName), Map[ColumnName, Column]],
                        pksByTable: Map[(SchemaName, TableName), Vector[Column]],
                        fks: Set[ForeignKey],
                        fksFromTable: Map[(SchemaName, TableName), Set[ForeignKey]],
                        fksToTable: Map[(SchemaName, TableName), Set[ForeignKey]])
