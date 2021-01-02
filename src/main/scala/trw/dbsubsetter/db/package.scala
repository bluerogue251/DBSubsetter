package trw.dbsubsetter

import trw.dbsubsetter.db.ColumnTypes.ColumnType

import java.sql.Connection

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
      val dataType: ColumnType
  )

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

  // Primary keys can be multi-column. Therefore a single primary key value is a sequence of individual column values.
  class PrimaryKeyValue(val individualColumnValues: Seq[Any])

  // Foreign keys can be multi-column. Therefore a single foreign key value is a sequence of individual column values.
  class ForeignKeyValue(val individualColumnValues: Seq[Any]) {
    val isEmpty: Boolean = individualColumnValues.forall(_ == null)
  }

  // Represents a single row from the origin database including all columns
  class Row(val data: Map[Column, Any])

  // Represents a single row from the origin database including only primary and foreign key columns
  class Keys(data: Map[Column, Any]) {

    def getValue(pk: PrimaryKey): PrimaryKeyValue = {
      val columnValues: Seq[Any] = pk.columns.map(data)
      new PrimaryKeyValue(columnValues)
    }

    def getValue(fk: ForeignKey, confusingTechDebt: Boolean): ForeignKeyValue = {
      val columns: Seq[Column] = if (confusingTechDebt) fk.toCols else fk.fromCols
      val columnValues: Seq[Any] = columns.map(data)
      new ForeignKeyValue(columnValues)
    }
  }

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
