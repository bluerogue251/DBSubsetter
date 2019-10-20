package trw.dbsubsetter.db.impl.origin

import io.prometheus.client.Histogram
import io.prometheus.client.Histogram.Timer
import trw.dbsubsetter.db.{ForeignKey, OriginDbAccess, Row, SqlQuery, Table}
import trw.dbsubsetter.metrics.Metrics

private[db] class InstrumentedOriginDbAccess(delegatee: OriginDbAccess) extends OriginDbAccess {

  private[this] val durationPerStatement: Histogram = Metrics.OriginDbDurationPerStatement

  private[this] val rowsFetchedPerStatement: Histogram = Metrics.OriginDbRowsFetchedPerStatement

  private[this] val durationPerRow: Histogram = Metrics.OriginDbDurationPerRow

  override def getRowsFromTemplate(fk: ForeignKey, table: Table, fkValue: Any): Vector[Row] = {
    instrument(() => delegatee.getRowsFromTemplate(fk, table, fkValue))
  }

  override def getRows(query: SqlQuery, table: Table): Vector[Row] = {
    instrument(() => delegatee.getRows(query, table))
  }

  private[this] def instrument(func: () => Vector[Row]): Vector[Row] = {
    val timer: Timer = durationPerStatement.startTimer()
    val result: Vector[Row] = func.apply()
    val statementDuration: Double = timer.observeDuration()
    rowsFetchedPerStatement.observe(result.length)
    if (result.nonEmpty) { // TODO test the divide by zero case
      durationPerRow.observe(statementDuration / result.length)
    }

    result
  }
}