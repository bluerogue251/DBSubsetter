package trw.dbsubsetter.db.impl.origin

import trw.dbsubsetter.db.impl.connection.ConnectionFactory
import trw.dbsubsetter.db.impl.mapper.JdbcResultConverter
import trw.dbsubsetter.db.{ForeignKey, OriginDbAccess, Row, SchemaInfo, Sql, SqlQuery, Table}

private[db] class OriginDbAccessImpl(connStr: String, sch: SchemaInfo, mapper: JdbcResultConverter, connectionFactory: ConnectionFactory) extends OriginDbAccess {

  private[this] val conn = connectionFactory.getReadOnlyConnection(connStr)

  private[this] val statements = Sql.preparedQueryStatementStrings(sch).map { case ((fk, table), sqlStr) =>
    (fk, table) -> conn.prepareStatement(sqlStr)
  }

  override def getRowsFromTemplate(fk: ForeignKey, table: Table, fkValue: Any): Vector[Row] = {
    val stmt = statements(fk, table)
    stmt.clearParameters()
    if (fk.isSingleCol) {
      stmt.setObject(1, fkValue)
    } else {
      fkValue.asInstanceOf[Array[Any]].zipWithIndex.foreach { case (value, i) =>
        stmt.setObject(i + 1, value)
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
