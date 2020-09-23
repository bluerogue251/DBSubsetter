package trw.dbsubsetter.db.impl.target

import io.prometheus.client.Histogram
import trw.dbsubsetter.db.Row
import trw.dbsubsetter.db.Table
import trw.dbsubsetter.db.TargetDbAccess
import trw.dbsubsetter.metrics.Metrics

private[db] class InstrumentedTargetDbAccess(delegatee: TargetDbAccess) extends TargetDbAccess {

  private[this] val durationPerStatement: Histogram = Metrics.TargetDbDurationPerStatement

  private[this] val rowsInsertedPerStatement: Histogram = Metrics.TargetDbRowsInsertedPerStatement

  private[this] val durationPerRow: Histogram = Metrics.TargetDbDurationPerRow

  // TODO institute a guarantee that this will never be called with empty rows
  override def insertRows(table: Table, rows: Vector[Row]): Unit = {
    val runnable: Runnable = () => delegatee.insertRows(table, rows)
    val statementDuration: Double = durationPerStatement.time(runnable)
    rowsInsertedPerStatement.observe(rows.size)
    if (rows.nonEmpty) {
      durationPerRow.observe(statementDuration / rows.size)
    }
  }
}
