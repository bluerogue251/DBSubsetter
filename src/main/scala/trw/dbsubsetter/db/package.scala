package trw.dbsubsetter

import java.sql.Connection

import trw.dbsubsetter.values.{ColumnValue, ColumnValueFactory, NullColumnValue}

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
      val valueFactory: ColumnValueFactory
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

  sealed trait PrimaryKeyValue
  case class SingleColumnPrimaryKeyValue(value: ColumnValue) extends PrimaryKeyValue
  case class MultiColumnPrimaryKeyValue(values: Seq[ColumnValue]) extends PrimaryKeyValue

  sealed trait ForeignKeyValue {
    def isEmpty: Boolean
  }
  case class SingleColumnForeignKeyValue(value: ColumnValue) extends ForeignKeyValue {
    val isEmpty: Boolean = value == NullColumnValue
  }
  case class MultiColumnForeignKeyValue(values: Seq[ColumnValue]) extends ForeignKeyValue {
    val isEmpty: Boolean = values.forall(_ == NullColumnValue)
  }

  // Represents a single row from the origin database including all columns
  class Row(val data: Array[Any])

  // Represents a single row from the origin database including only primary and foreign key columns
  class Keys(data: Array[Any]) {
    def getValue(pk: PrimaryKey): PrimaryKeyValue = {
      val columnValues: Seq[ColumnValue] =
        pk.columns.map { col =>
          val rawValue: Any = data(col.keyOrdinalPosition)
          col.valueFactory.fromRaw(rawValue)
        }

      if (columnValues.size == 1)
        SingleColumnPrimaryKeyValue(columnValues.head)
      else
        MultiColumnPrimaryKeyValue(columnValues)
    }

    def getValue(fk: ForeignKey, confusingTechDebt: Boolean): ForeignKeyValue = {
      val columns: Seq[Column] =
        if (confusingTechDebt)
          fk.toCols
        else
          fk.fromCols

      val columnValues: Seq[ColumnValue] =
        columns.map { column =>
          val rawValue: Any = data(column.keyOrdinalPosition)
          column.valueFactory.fromRaw(rawValue)
        }

      if (columnValues.size == 1)
        SingleColumnForeignKeyValue(columnValues.head)
      else
        MultiColumnForeignKeyValue(columnValues)

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
