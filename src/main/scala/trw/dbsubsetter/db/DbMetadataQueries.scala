package trw.dbsubsetter.db

import java.sql.{DriverManager, JDBCType}

import trw.dbsubsetter.config.Config

import scala.collection.mutable.ArrayBuffer

private[db] object DbMetadataQueries {
  def queryDb(config: Config): DbMetadataQueryResult = {
    val conn = DriverManager.getConnection(config.originDbConnectionString)
    conn.setReadOnly(true)
    val catalog = conn.getCatalog
    val ddl = conn.getMetaData

    val tables = ArrayBuffer.empty[TableQueryRow]
    val columns = ArrayBuffer.empty[ColumnQueryRow]
    val pks = ArrayBuffer.empty[PrimaryKeyQueryRow]
    val fks = ArrayBuffer.empty[ForeignKeyQueryRow]

    //
    // Get all tables for configured schemas
    //
    config.schemas.foreach { schema =>
      val tablesJdbcResultSet = {
        if (conn.isMysql) ddl.getTables(schema, null, "%", Array("TABLE"))
        else ddl.getTables(catalog, schema, "%", Array("TABLE"))
      }
      while (tablesJdbcResultSet.next()) {
        val table = tablesJdbcResultSet.getString("TABLE_NAME")
        if (!config.excludeTables.contains((schema, table))) {
          tables += TableQueryRow(schema, table)
        }
      }
    }

    //
    // Fetch columns from DB
    //
    tables.foreach { table =>
      val colsJdbcResultSet = {
        if (conn.isMysql) ddl.getColumns(table.schema, null, table.name, "%")
        else ddl.getColumns(catalog, table.schema, table.name, "%")
      }
      while (colsJdbcResultSet.next()) {
        val columnName = colsJdbcResultSet.getString("COLUMN_NAME")
        val jdbcType = JDBCType.valueOf(colsJdbcResultSet.getInt("DATA_TYPE"))
        val typeName = colsJdbcResultSet.getString("TYPE_NAME")
        // If trying to generalize autoincrement across vendors, check if "YES" is what other vendors use
        // Or if it's something else like "y", "1", "true" etc.
        val isSqlServerAutoIncrement = conn.isMsSqlServer && colsJdbcResultSet.getString("IS_AUTOINCREMENT") == "YES"

        if (!config.excludeColumns((table.schema, table.name)).contains(columnName)) {
          columns += ColumnQueryRow(table.schema, table.name, columnName, jdbcType, typeName, isSqlServerAutoIncrement)
        }
      }
    }

    //
    // Fetch primary keys from DB
    //
    tables.foreach { table =>
      val pksJdbcResultSet = {
        if (conn.isMysql) ddl.getPrimaryKeys(table.schema, null, table.name)
        else ddl.getPrimaryKeys(catalog, table.schema, table.name)
      }
      while (pksJdbcResultSet.next()) {
        pks += PrimaryKeyQueryRow(
          table.schema,
          table.name,
          pksJdbcResultSet.getString("COLUMN_NAME")
        )
      }
    }

    //
    // Add primary keys configured by user
    //
    config.cmdLinePrimaryKeys.foreach { clpk =>
      clpk.columns.foreach(c => pks += PrimaryKeyQueryRow(clpk.schema, clpk.table, c))
    }

    //
    // Fetch foreign keys from DB
    //
    tables.foreach { table =>
      val foreignKeysJdbcResultSet = {
        if (conn.isMysql) ddl.getImportedKeys(table.schema, null, table.name)
        else ddl.getImportedKeys(catalog, table.schema, table.name)
      }
      while (foreignKeysJdbcResultSet.next()) {
        val fromSchema = if (conn.isMysql) foreignKeysJdbcResultSet.getString("FKTABLE_CAT") else foreignKeysJdbcResultSet.getString("FKTABLE_SCHEM")
        val fromTable = foreignKeysJdbcResultSet.getString("FKTABLE_NAME")
        val fromColumn = foreignKeysJdbcResultSet.getString("FKCOLUMN_NAME")

        val toSchema = if (conn.isMysql) foreignKeysJdbcResultSet.getString("PKTABLE_CAT") else foreignKeysJdbcResultSet.getString("PKTABLE_SCHEM")
        val toTable = foreignKeysJdbcResultSet.getString("PKTABLE_NAME")
        val toColumn = foreignKeysJdbcResultSet.getString("PKCOLUMN_NAME")

        if (!config.excludeTables.contains((fromSchema, fromTable)) && !config.excludeTables.contains((toSchema, toTable))) {
          fks += ForeignKeyQueryRow(fromSchema, fromTable, fromColumn, toSchema, toTable, toColumn)
        }
      }
    }

    //
    // Add foreign keys configured by user
    //
    config.cmdLineForeignKeys.foreach { cfk =>
      cfk.fromColumns.zip(cfk.toColumns).foreach { case (fromCol, toCol) =>
        fks += ForeignKeyQueryRow(cfk.fromSchema, cfk.fromTable, fromCol, cfk.toSchema, cfk.toTable, toCol)
      }
    }

    //
    // Fetch DB Vendor info
    //
    val dbVendor = conn.dbVendor

    //
    // Close DB connection
    //
    conn.close()

    DbMetadataQueryResult(tables.toVector, columns.toVector, pks.toVector, fks.toVector, dbVendor)
  }
}

private[db] case class DbMetadataQueryResult(tables: Vector[TableQueryRow],
                                             columns: Vector[ColumnQueryRow],
                                             pks: Vector[PrimaryKeyQueryRow],
                                             fks: Vector[ForeignKeyQueryRow],
                                             vendor: DbVendor)

private[this] case class TableQueryRow(schema: SchemaName,
                                       name: TableName)

private[this] case class ColumnQueryRow(schema: SchemaName,
                                        table: TableName,
                                        name: ColumnName,
                                        jdbcType: JDBCType,
                                        typeName: String,
                                        isSqlServerAutoincrement: Boolean)

private[this] case class PrimaryKeyQueryRow(schema: SchemaName,
                                            table: TableName,
                                            column: ColumnName)

private[this] case class ForeignKeyQueryRow(fromSchema: SchemaName,
                                            fromTable: TableName,
                                            fromColumn: ColumnName,
                                            toSchema: SchemaName,
                                            toTable: TableName,
                                            toColumn: ColumnName)
