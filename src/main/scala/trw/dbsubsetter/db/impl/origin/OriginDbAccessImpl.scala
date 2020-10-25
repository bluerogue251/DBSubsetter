package trw.dbsubsetter.db.impl.origin

import java.sql.PreparedStatement

import trw.dbsubsetter.db._
import trw.dbsubsetter.db.impl.ConnectionFactory
import trw.dbsubsetter.db.impl.mapper.JdbcResultConverter

private[db] class OriginDbAccessImpl(
    connStr: String,
    schemaInfo: SchemaInfo,
    mapper: JdbcResultConverter,
    connectionFactory: ConnectionFactory
) extends OriginDbAccess {

  private[this] val conn = connectionFactory.getReadOnlyConnection(connStr)

  private[this] val foreignKeyTemplateStatements: Map[(ForeignKey, Table), PreparedStatement] =
    Sql.queryByFkSqlTemplates(schemaInfo).data.map { case ((fk, table), sqlQuery) =>
      (fk, table) -> conn.prepareStatement(sqlQuery.value)
    }

  private[this] val primaryKeyTemplateStatements: Map[(Table, Short), PreparedStatement] =
    Sql.queryByPkSqlTemplates(schemaInfo).data.map { case (tableWithBatchSize, sqlQuery) =>
      tableWithBatchSize -> conn.prepareStatement(sqlQuery.value)
    }

  override def getRowsFromForeignKeyValue(fk: ForeignKey, table: Table, fkValue: ForeignKeyValue): Vector[Keys] = {
    val stmt = foreignKeyTemplateStatements(fk, table)
    stmt.clearParameters()

    fkValue.individualColumnValues.zipWithIndex.foreach { case (value, i) =>
      stmt.setBytes(i + 1, value)
    }

    val jdbcResult = stmt.executeQuery()
    mapper.convertToKeys(jdbcResult, table)
  }

  override def getRowsFromPrimaryKeyValues(table: Table, primaryKeyValues: Seq[PrimaryKeyValue]): Vector[Row] = {
    val stmt = primaryKeyTemplateStatements((table, primaryKeyValues.size.toShort))
    stmt.clearParameters()

    var i: Int = 1
    primaryKeyValues.foreach { primaryKeyValue =>
      primaryKeyValue.individualColumnValues.foreach { individualColumnValue =>
        stmt.setBytes(i, individualColumnValue)
        i += 1
      }
    }

    val jdbcResult = stmt.executeQuery()
    mapper.convertToRows(jdbcResult, table)
  }

  override def getRowsFromWhereClause(table: Table, whereClause: String): Vector[Keys] = {
    val selectColumns = schemaInfo.keyColumnsByTableOrdered(table)
    val sqlString = Sql.makeQueryString(table, selectColumns, whereClause)
    val jdbcResult = conn.createStatement().executeQuery(sqlString.value)
    mapper.convertToKeys(jdbcResult, table)
  }
}
