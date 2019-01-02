package trw.dbsubsetter.db.impl.mapper

import java.sql.ResultSet

import trw.dbsubsetter.db.{Row, Table}
import trw.dbsubsetter.metrics.Metrics

private[db] class JdbcResultConverterTimed(delegatee: JdbcResultConverter) extends JdbcResultConverter {

  private[this] val metrics = Metrics.JdbcResultConverterHistogram

  def convert(jdbcResultSet: ResultSet, table: Table): Vector[Row] = {
    metrics.time(() => delegatee.convert(jdbcResultSet, table))
  }

}
