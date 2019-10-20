package trw.dbsubsetter.db.impl.target

import io.prometheus.client.Histogram
import trw.dbsubsetter.db.{Row, Table, TargetDbAccess}
import trw.dbsubsetter.metrics.Metrics

private[db] class InstrumentedTargetDbAccess(delegatee: TargetDbAccess) extends TargetDbAccess {

  private[this] val durationPerStatement: Histogram = Metrics.TargetDbDurationPerStatement

  private[this] val rowsInsertedPerStatement: Histogram = Metrics.TargetDbRowsInsertedPerStatement

  private[this] val durationPerRow: Histogram = Metrics.TargetDbDurationPerRow

  // TODO institute a guarantee that this will never be called with empty rows
  override def insertRows(table: Table, rows: Vector[Row]): Unit = {
    val runnable: Runnable = () => delegatee.insertRows(table, rows)
    rowsInsertedPerStatement.observe(rows.size)
    val statementDuration: Double = durationPerStatement.time(runnable)
    if (rows.nonEmpty) {
      durationPerRow.observe(statementDuration / rows.size)
    }
  }
}
