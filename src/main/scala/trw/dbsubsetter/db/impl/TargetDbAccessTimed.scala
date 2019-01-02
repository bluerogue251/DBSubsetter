package trw.dbsubsetter.db.impl

import io.prometheus.client.Histogram
import trw.dbsubsetter.db.{Row, Table, TargetDbAccess}

private[db] class TargetDbAccessTimed(delegatee: TargetDbAccess) extends TargetDbAccess {

  private val metrics = TargetDbAccessTimed.Metrics

  override def insertRows(table: Table, rows: Vector[Row]): Int = {
    metrics.time(() => delegatee.insertRows(table, rows))
  }
}

private object TargetDbAccessTimed {
  private val Metrics: Histogram = Histogram
    .build()
    .name("TargetDbInserts")
    .help("n/a")
    .register()
}