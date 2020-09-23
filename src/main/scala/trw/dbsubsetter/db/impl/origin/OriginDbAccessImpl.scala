package trw.dbsubsetter.db.impl.origin

import java.sql.PreparedStatement

import trw.dbsubsetter.db.impl.ConnectionFactory
import trw.dbsubsetter.db.impl.mapper.JdbcResultConverter
import trw.dbsubsetter.db.ForeignKey
import trw.dbsubsetter.db.ForeignKeyValue
import trw.dbsubsetter.db.Keys
import trw.dbsubsetter.db.OriginDbAccess
import trw.dbsubsetter.db.PrimaryKeyValue
import trw.dbsubsetter.db.Row
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.db.Sql
import trw.dbsubsetter.db.SqlQuery
import trw.dbsubsetter.db.Table

private[db] class OriginDbAccessImpl(
    connStr: String,
    sch: SchemaInfo,
    mapper: JdbcResultConverter,
    connectionFactory: ConnectionFactory
) extends OriginDbAccess {

  private[this] val conn = connectionFactory.getReadOnlyConnection(connStr)

  private[this] val foreignKeyTemplateStatements: Map[(ForeignKey, Table), PreparedStatement] =
    Sql.queryByFkSqlTemplates(sch).data.map { case ((fk, table), sqlQuery) =>
      (fk, table) -> conn.prepareStatement(sqlQuery.value)
    }

  private[this] val primaryKeyTemplateStatements: Map[(Table, Short), PreparedStatement] =
    Sql.queryByPkSqlTemplates(sch).data.map { case (tableWithBatchSize, sqlQuery) =>
      tableWithBatchSize -> conn.prepareStatement(sqlQuery.value)
    }

  override def getRowsFromForeignKeyValue(fk: ForeignKey, table: Table, fkValue: ForeignKeyValue): Vector[Keys] = {
    val stmt = foreignKeyTemplateStatements(fk, table)
    stmt.clearParameters()

    fkValue.individualColumnValues.zipWithIndex.foreach { case (value, i) =>
      stmt.setObject(i + 1, value)
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
        stmt.setObject(i, individualColumnValue)
        i += 1
      }
    }

    val jdbcResult = stmt.executeQuery()
    mapper.convertToRows(jdbcResult, table)
  }

  override def getRows(query: SqlQuery, table: Table): Vector[Keys] = {
    val jdbcResult = conn.createStatement().executeQuery(query.value)
    mapper.convertToKeys(jdbcResult, table)
  }
}
