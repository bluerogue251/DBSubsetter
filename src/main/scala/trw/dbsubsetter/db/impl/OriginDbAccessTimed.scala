package trw.dbsubsetter.db.impl

import io.prometheus.client.Histogram
import trw.dbsubsetter.db.{ForeignKey, OriginDbAccess, Row, SqlQuery, Table}

private[db] class OriginDbAccessTimed(delegatee: OriginDbAccess) extends OriginDbAccess {

  private val metrics = OriginDbAccessTimed.Metrics

  override def getRowsFromTemplate(fk: ForeignKey, table: Table, fkValue: Any): Vector[Row] = {
    metrics.time(() =>  delegatee.getRowsFromTemplate(fk, table, fkValue))
  }

  override def getRows(query: SqlQuery, table: Table): Vector[Row] = {
    metrics.time(() => delegatee.getRows(query, table))
  }
}

private object OriginDbAccessTimed {
  private val Metrics: Histogram = Histogram
    .build()
    .name("OriginDbSelects")
    .help("n/a")
    .register()
}