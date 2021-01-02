package trw.dbsubsetter

import trw.dbsubsetter.db.value.{ColumnValue, KeyValue}

import java.sql.{Connection, ResultSet}

package object db {

  case class Schema(name: String)

  case class Table(schema: Schema, name: String)

  case class SqlQuery(value: String)

  case class ForeignKeySqlTemplates(data: Map[(ForeignKey, Table), SqlQuery])

  case class PrimaryKeySqlTemplates(data: Map[(Table, Short), SqlQuery])

  class SchemaInfo(
      val tables: Seq[TableWithAutoincrementMetadata],
      // Only those columns involved in a primary or foreign key
      val keyColumnsByTable: Map[Table, Seq[Column]],
      // All columns, even those uninvolved in a primary or foreign key
      val dataColumnsByTable: Map[Table, Seq[Column]],
      val pksByTable: Map[Table, PrimaryKey],
      val fksOrdered: Array[ForeignKey],
      val fksFromTable: Map[Table, Vector[ForeignKey]],
      val fksToTable: Map[Table, Vector[ForeignKey]]
  )

  case class TableWithAutoincrementMetadata(
      table: Table,
      hasSqlServerAutoIncrement: Boolean
  )

  class Column(
      val table: Table,
      val name: String,
      val getValue: Function[ResultSet, ColumnValue]
  ) {
    def getValueMaybe(rs: ResultSet): Option[ColumnValue] = {
      val columnValue: ColumnValue = getValue(rs)
      if (rs.wasNull()) None else Some(columnValue)
    }
  }

  class PrimaryKey(val columns: Seq[Column])

  class ForeignKey(
      val fromCols: Seq[Column],
      val toCols: Seq[Column],
      val pointsToPk: Boolean,
      var i: Short
  ) {
    val fromTable: Table = fromCols.head.table
    val toTable: Table = toCols.head.table

    // TODO Refactor to remove mutability
    def setIndex(index: Short): Unit = {
      i = index
    }
  }

  class PrimaryKeyValue(val x: KeyValue)
  class ForeignKeyValue(val x: KeyValue)

  // Represents a single row from the origin database including all columns
  class Row(val data: Map[Column, Any])

  // Represents a single row from the origin database including only primary and foreign key values
  class Keys(val pkValue: PrimaryKeyValue, val fkValues: Map[(ForeignKey, Boolean), ForeignKeyValue])

  implicit class VendorAwareJdbcConnection(private val conn: Connection) {
    private val vendorName: String = conn.getMetaData.getDatabaseProductName

    val dbVendor: DbVendor = vendorName match {
      case "Microsoft SQL Server" => DbVendor.MicrosoftSQLServer
      case "MySQL"                => DbVendor.MySQL
      case "PostgreSQL"           => DbVendor.PostgreSQL
    }

    def isMysql: Boolean = dbVendor == DbVendor.MySQL

    def isMsSqlServer: Boolean = dbVendor == DbVendor.MicrosoftSQLServer
  }

}
