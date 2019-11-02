package trw.dbsubsetter.db.impl.origin

import java.sql.PreparedStatement

import trw.dbsubsetter.db.impl.connection.ConnectionFactory
import trw.dbsubsetter.db.impl.mapper.JdbcResultConverter
import trw.dbsubsetter.db.{ForeignKey, ForeignKeyValue, OriginDbAccess, PrimaryKeyValue, Row, SchemaInfo, Sql, SqlQuery, Table}


// TODO fix this so the line is shorter and re-enable scalastyle
// scalastyle:off
private[db] class OriginDbAccessImpl(connStr: String, sch: SchemaInfo, mapper: JdbcResultConverter, connectionFactory: ConnectionFactory) extends OriginDbAccess {
// scalastyle:on

  private[this] val conn = connectionFactory.getReadOnlyConnection(connStr)

  private[this] val foreignKeyTemplateStatements: Map[(ForeignKey, Table), PreparedStatement] =
    Sql.queryByFkSqlTemplates(sch).map { case ((fk, table), sqlString) =>
      (fk, table) -> conn.prepareStatement(sqlString)
    }

  private[this] val primaryKeyTemplateStatements: Map[(Table, Short), PreparedStatement] =
    Sql.queryByPkSqlTemplates(sch).map { case (tableWithBatchSize, sqlString) =>
      tableWithBatchSize -> conn.prepareStatement(sqlString)
    }

  override def getRowsFromForeignKeyValue(fk: ForeignKey, table: Table, fkValue: ForeignKeyValue): Vector[Row] = {
    val stmt = foreignKeyTemplateStatements(fk, table)
    stmt.clearParameters()

    fkValue.individualColumnValues.zipWithIndex.foreach { case (value, i) =>
      stmt.setObject(i + 1, value)
    }

    val jdbcResult = stmt.executeQuery()
    mapper.convert(jdbcResult, table)
  }

  override def getRowsFromPrimaryKeyValues(table: Table, primaryKeyValues: Seq[PrimaryKeyValue]): Vector[Row] = {
    val stmt = primaryKeyTemplateStatements((table, primaryKeyValues.size.toShort))
    stmt.clearParameters()

    var i: Int = 1
    primaryKeyValues.foreach { primaryKeyValue =>
      primaryKeyValue.individualColumnValues.foreach { individualColumnValue =>
        stmt.setObject(i, individualColumnValue)
        i += 1
      }
    }

    val jdbcResult = stmt.executeQuery()
    mapper.convert(jdbcResult, table)
  }

  override def getRows(query: SqlQuery, table: Table): Vector[Row] = {
    val jdbcResult = conn.createStatement().executeQuery(query)
    mapper.convert(jdbcResult, table)
  }
}
