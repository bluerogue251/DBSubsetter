package trw.dbsubsetter.db.impl

import trw.dbsubsetter.db.{Row, Table, TargetDbAccess}
import trw.dbsubsetter.metrics.Metrics

private[db] class TargetDbAccessTimed(delegatee: TargetDbAccess) extends TargetDbAccess {

  private[this] val metrics = Metrics.TargetDbInsertsHistogram

  override def insertRows(table: Table, rows: Vector[Row]): Int = {
    metrics.time(() => delegatee.insertRows(table, rows))
  }

}