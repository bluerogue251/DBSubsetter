package trw.dbsubsetter.db.impl.target

import io.prometheus.client.Histogram
import trw.dbsubsetter.db.{Row, Table, TargetDbAccess}
import trw.dbsubsetter.metrics.Metrics

private[db] class InstrumentedTargetDbAccess(delegatee: TargetDbAccess) extends TargetDbAccess {

  private[this] val rowsInsertedPerStatement: Histogram = Metrics.TargetDbRowsInsertedPerStatement

  private[this] val durationPerStatement: Histogram = Metrics.TargetDbDurationPerStatement

  override def insertRows(table: Table, rows: Vector[Row]): Unit = {
    rowsInsertedPerStatement.observe(rows.size)
    val runnable: Runnable = () => delegatee.insertRows(table, rows)
    durationPerStatement.time(runnable)
  }
}
