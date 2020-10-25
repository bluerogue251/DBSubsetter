package trw.dbsubsetter

import java.sql.Connection

import trw.dbsubsetter.db.ColumnTypes.ColumnType

package object db {

  case class Schema(name: String)

  case class Table(schema: Schema, name: String)

  case class SqlQuery(value: String)

  case class ForeignKeySqlTemplates(data: Map[(ForeignKey, Table), SqlQuery])

  case class PrimaryKeySqlTemplates(data: Map[(Table, Short), SqlQuery])

  class SchemaInfo(
      val tables: Seq[TableWithAutoincrementMetadata],
      // Only those columns involved in a primary or foreign key
      val keyColumnsByTableOrdered: Map[Table, Vector[Column]],
      // All columns, even those uninvolved in a primary or foreign key
      val dataColumnsByTableOrdered: Map[Table, Vector[Column]],
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
      /*
       * The 0-indexed location of this column in query results where only primary and foreign key columns are included
       * -1 if this column is not part of a primary or foreign key, as this column would not be included in that query.
       * TODO make this immutable
       */
      var keyOrdinalPosition: Int,
      // The 0-indexed location of this column in query results where all columns are included
      val dataOrdinalPosition: Int,
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
  class PrimaryKeyValue(val individualColumnValues: Seq[Array[Byte]])

  // Foreign keys can be multi-column. Therefore a single foreign key value is a sequence of individual column values.
  class ForeignKeyValue(val individualColumnValues: Seq[Array[Byte]]) {
    val isEmpty: Boolean = individualColumnValues.forall(_ == null)
  }

  // Represents a single row from the origin database including all columns
  class Row(val data: Array[Array[Byte]])

  // Represents a single row from the origin database including only primary and foreign key columns
  class Keys(data: Array[Array[Byte]]) {

    def getValue(pk: PrimaryKey): PrimaryKeyValue = {
      val individualColumnValues: Seq[Array[Byte]] = pk.columns.map(_.keyOrdinalPosition).map(data)
      new PrimaryKeyValue(individualColumnValues)
    }

    def getValue(fk: ForeignKey, confusingTechDebt: Boolean): ForeignKeyValue = {
      val columns: Seq[Column] = if (confusingTechDebt) fk.toCols else fk.fromCols
      val individualColumnOrdinals: Seq[Int] = columns.map(_.keyOrdinalPosition)
      val individualColumnValues: Seq[Array[Byte]] = individualColumnOrdinals.map(data)
      new ForeignKeyValue(individualColumnValues)
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
