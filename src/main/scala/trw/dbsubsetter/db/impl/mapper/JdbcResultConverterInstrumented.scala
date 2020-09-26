package trw.dbsubsetter.db.impl.mapper

import java.sql.ResultSet

import trw.dbsubsetter.db.{Keys, Row, Table}
import trw.dbsubsetter.metrics.Metrics

private[db] class JdbcResultConverterInstrumented(base: JdbcResultConverter) extends JdbcResultConverter {

  private[this] val metrics = Metrics.JdbcResultConverterHistogram

  def convertToKeys(jdbcResultSet: ResultSet, table: Table): Vector[Keys] = {
    metrics.time(() => base.convertToKeys(jdbcResultSet, table))
  }

  override def convertToRows(jdbcResultSet: ResultSet, table: Table): Vector[Row] = {
    metrics.time(() => base.convertToRows(jdbcResultSet, table))
  }
}
