package trw.dbsubsetter.db.impl.origin

import java.sql.PreparedStatement

import trw.dbsubsetter.db.impl.connection.ConnectionFactory
import trw.dbsubsetter.db.impl.mapper.JdbcResultConverter
import trw.dbsubsetter.db.{ForeignKey, OriginDbAccess, PrimaryKeyValue, Row, SchemaInfo, Sql, SqlQuery, Table}

// scalastyle:off
private[db] class OriginDbAccessImpl(connStr: String, sch: SchemaInfo, mapper: JdbcResultConverter, connectionFactory: ConnectionFactory) extends OriginDbAccess {
// scalastyle:on

  private[this] val conn = connectionFactory.getReadOnlyConnection(connStr)

  private[this] val foreignKeyTemplateStatements: Map[(ForeignKey, Table), PreparedStatement] =
    Sql.preparedQueryStatementStrings(sch).map { case ((fk, table), sqlString) =>
      (fk, table) -> conn.prepareStatement(sqlString)
    }

  private[this] val primaryKeyTemplateStatements: Map[Table, PreparedStatement] =
    Sql.preparedQueryByPrimaryKeyStatementStrings(sch).map { case (table, sqlString) =>
      table -> conn.prepareStatement(sqlString)
    }

  override def getRowsFromForeignKeyValue(fk: ForeignKey, table: Table, fkValue: Any): Vector[Row] = {
    val stmt = foreignKeyTemplateStatements(fk, table)
    stmt.clearParameters()

    /*
     * TODO experiment and see if the check for isSingleCol makes any performance difference in practice. If not,
     *   consider just treating everything as an array. Same possibility in a few other places as well.
     */
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

  override def getRowsFromPrimaryKeyValues(table: Table, primaryKeyValues: Seq[PrimaryKeyValue]): Vector[Row] = {
    val stmt = primaryKeyTemplateStatements(table)
    stmt.clearParameters()

    primaryKeyValues.foreach(primaryKeyValue => {
      primaryKeyValue.individualColumnValues.zipWithIndex.foreach { case (individualColumnValue, i) =>
          stmt.setObject(i + 1, individualColumnValue)
      }
    })

    val jdbcResult = stmt.executeQuery()
    mapper.convert(jdbcResult, table)
  }

  override def getRows(query: SqlQuery, table: Table): Vector[Row] = {
    val jdbcResult = conn.createStatement().executeQuery(query)
    mapper.convert(jdbcResult, table)
  }
}
