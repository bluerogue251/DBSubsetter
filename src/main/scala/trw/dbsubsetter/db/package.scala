package trw.dbsubsetter

import java.sql.Connection

import trw.dbsubsetter.db.ColumnTypes.ColumnType

package object db {
  type SchemaName = String
  type TableName = String
  type ColumnName = String
  type WhereClause = String
  type TypeName = String
  type SqlQuery = String
  type ForeignKeySqlTemplates = Map[(ForeignKey, Table), SqlQuery]
  type PrimaryKeySqlTemplates = Map[(Table, Short), SqlQuery]

  class SchemaInfo(
    val tablesByName: Map[(SchemaName, TableName), Table],
    // Only those columns involved in a primary or foreign key
    val keyColumnsByTableOrdered: Map[Table, Vector[Column]],
    // All columns, even those uninvolved in a primary or foreign key
    val dataColumnsByTableOrdered: Map[Table, Vector[Column]],
    val pksByTable: Map[Table, PrimaryKey],
    val fksOrdered: Array[ForeignKey],
    val fksFromTable: Map[Table, Vector[ForeignKey]],
    val fksToTable: Map[Table, Vector[ForeignKey]]
  )

  class Table(
    val schema: SchemaName,
    val name: TableName,
    val hasSqlServerAutoIncrement: Boolean
  )

  class Column(
    val table: Table,
    val name: ColumnName,
    /*
     * The 0-indexed location of this column in query results where only primary and foreign key columns are included
     * -1 if this column is not part of a primary or foreign key, as this column would not be included in that query
     */
    val keyOrdinalPosition: Int,
    // The 0-indexed location of this column in query results where all columns are included
    val dataOrdinalPosition: Int,
    val dataType: ColumnType
  )

  class PrimaryKey(val columns: Seq[Column])

  class ForeignKey(
    val fromCols: Vector[Column],
    val toCols: Vector[Column],
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
  class Row(val data: Array[Any])

  // Represents a single row from the origin database including only primary and foreign key columns
  class Keys(data: Array[Any]) {
    def getValue(pk: PrimaryKey): PrimaryKeyValue = {
      val individualColumnValues: Seq[Any] = pk.columns.map(_.keyOrdinalPosition).map(data)
      new PrimaryKeyValue(individualColumnValues)
    }

    def getValue(fk: ForeignKey, confusingTechDebt: Boolean): ForeignKeyValue = {
      val columns: Seq[Column] = if (confusingTechDebt) fk.toCols else fk.fromCols
      val individualColumnOrdinals: Seq[Int] = columns.map(_.keyOrdinalPosition)
      val individualColumnValues: Seq[Any] = individualColumnOrdinals.map(data)
      new ForeignKeyValue(individualColumnValues)
    }
  }

  implicit class VendorAwareJdbcConnection(private val conn: Connection) {
    private val vendorName: String = conn.getMetaData.getDatabaseProductName

    val dbVendor: DbVendor = vendorName match {
      case "Microsoft SQL Server" => DbVendor.MicrosoftSQLServer
      case "MySQL" => DbVendor.MySQL
      case "PostgreSQL" => DbVendor.PostgreSQL
    }

    def isMysql: Boolean = dbVendor == DbVendor.MySQL

    def isMsSqlServer: Boolean = dbVendor == DbVendor.MicrosoftSQLServer
  }
}
