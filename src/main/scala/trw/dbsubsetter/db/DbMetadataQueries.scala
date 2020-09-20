package trw.dbsubsetter.db

import java.sql.{DriverManager, JDBCType}

import scala.collection.mutable.ArrayBuffer

// scalastyle:off
object DbMetadataQueries {
  def retrieveSchemaMetadata(connectionString: String, schemas: Seq[Schema]): DbMetadataQueryResult = {
    val conn = DriverManager.getConnection(connectionString)
    conn.setReadOnly(true)
    val catalog = conn.getCatalog
    val ddl = conn.getMetaData

    val tables = ArrayBuffer.empty[TableQueryRow]
    val columns = ArrayBuffer.empty[ColumnQueryRow]
    val pks = ArrayBuffer.empty[PrimaryKeyColumnQueryRow]
    val fks = ArrayBuffer.empty[ForeignKeyColumnQueryRow]

    /*
     * Retrieve table metadata
     */
    schemas.foreach { schema =>
      val tablesJdbcResultSet =
        if (conn.isMysql) {
          ddl.getTables(schema.name, null, "%", Array("TABLE"))
        } else {
          ddl.getTables(catalog, schema.name, "%", Array("TABLE"))
        }

      while (tablesJdbcResultSet.next()) {
        val tableName = tablesJdbcResultSet.getString("TABLE_NAME")
        tables += TableQueryRow(schema.name, tableName)
      }
    }

    /*
     * Retrieve column metadata
     */
    tables.foreach { table =>
      val colsJdbcResultSet =
        if (conn.isMysql) {
          ddl.getColumns(table.schema, null, table.name, "%")
        } else {
          ddl.getColumns(catalog, table.schema, table.name, "%")
        }

      while (colsJdbcResultSet.next()) {
        val columnName = colsJdbcResultSet.getString("COLUMN_NAME")
        val jdbcType = JDBCType.valueOf(colsJdbcResultSet.getInt("DATA_TYPE"))
        val typeName = colsJdbcResultSet.getString("TYPE_NAME")
        // If trying to generalize autoincrement across vendors, check if "YES" is what other vendors use
        // Or if it's something else like "y", "1", "true" etc.
        val isSqlServerAutoIncrement = conn.isMsSqlServer && colsJdbcResultSet.getString("IS_AUTOINCREMENT") == "YES"
        columns += ColumnQueryRow(table.schema, table.name, columnName, jdbcType, typeName, isSqlServerAutoIncrement)
      }
    }

    /*
     * Retrieve primary key metadata
     */
    tables.foreach { table =>
      val pksJdbcResultSet =
        if (conn.isMysql) {
          ddl.getPrimaryKeys(table.schema, null, table.name)
        } else {
          ddl.getPrimaryKeys(catalog, table.schema, table.name)
        }

      while (pksJdbcResultSet.next()) {
        pks += PrimaryKeyColumnQueryRow(
          table.schema,
          table.name,
          pksJdbcResultSet.getString("COLUMN_NAME")
        )
      }
    }

    /*
     * Retrieve foreign key metadata
     */
    tables.foreach { table =>
      val foreignKeysJdbcResultSet =
        if (conn.isMysql) {
          ddl.getImportedKeys(table.schema, null, table.name)
        } else {
          ddl.getImportedKeys(catalog, table.schema, table.name)
        }

      while (foreignKeysJdbcResultSet.next()) {
        val fromSchema = if (conn.isMysql) foreignKeysJdbcResultSet.getString("FKTABLE_CAT") else foreignKeysJdbcResultSet.getString("FKTABLE_SCHEM")
        val fromTable = foreignKeysJdbcResultSet.getString("FKTABLE_NAME")
        val fromColumn = foreignKeysJdbcResultSet.getString("FKCOLUMN_NAME")

        val toSchema = if (conn.isMysql) foreignKeysJdbcResultSet.getString("PKTABLE_CAT") else foreignKeysJdbcResultSet.getString("PKTABLE_SCHEM")
        val toTable = foreignKeysJdbcResultSet.getString("PKTABLE_NAME")
        val toColumn = foreignKeysJdbcResultSet.getString("PKCOLUMN_NAME")
        fks += ForeignKeyColumnQueryRow(fromSchema, fromTable, fromColumn, toSchema, toTable, toColumn)
      }
    }

    /*
     * Fetch DB Vendor info
     */
    val dbVendor = conn.dbVendor

    /*
     * Close DB connection
     */
    conn.close()

    DbMetadataQueryResult(
      tables.toVector,
      columns.toVector,
      pks.toVector,
      fks.toVector,
      dbVendor
    )
  }
}

private[db] case class DbMetadataQueryResult(
    tables: Vector[TableQueryRow],
    columns: Vector[ColumnQueryRow],
    primaryKeyColumns: Vector[PrimaryKeyColumnQueryRow],
    foreignKeyColumns: Vector[ForeignKeyColumnQueryRow],
    vendor: DbVendor
)

private[this] case class TableQueryRow(
    schema: String,
    name: String
)

private[this] case class ColumnQueryRow(
    schema: String,
    table: String,
    name: String,
    jdbcType: JDBCType,
    typeName: String,
    isSqlServerAutoincrement: Boolean
)

private[this] case class PrimaryKeyColumnQueryRow(
    schema: String,
    table: String,
    column: String
)

private[this] case class ForeignKeyColumnQueryRow(
    fromSchema: String,
    fromTable: String,
    fromColumn: String,
    toSchema: String,
    toTable: String,
    toColumn: String
)
