package trw.dbsubsetter.db.impl.workaroundtest

import io.prometheus.client.{Counter, Histogram}
import trw.dbsubsetter.db.{Row, Table, TargetDbAccess}
import trw.dbsubsetter.metrics.Metrics

private[db] class InstrumentedTargetDbAccess(delegatee: TargetDbAccess) extends TargetDbAccess {

  private[this] val histogram: Histogram = Metrics.TargetDbInsertsHistogram

  private[this] val rowCounter: Counter = Metrics.TargetDbRowsInserted

  override def insertRows(table: Table, rows: Vector[Row]): Int = {
    rowCounter.inc(rows.size)
    histogram.time(() => delegatee.insertRows(table, rows))
  }
}