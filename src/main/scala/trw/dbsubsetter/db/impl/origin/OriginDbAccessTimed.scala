package trw.dbsubsetter.db.impl.origin

import trw.dbsubsetter.db.{ForeignKey, OriginDbAccess, Row, SqlQuery, Table}
import trw.dbsubsetter.metrics.Metrics

private[db] class OriginDbAccessTimed(delegatee: OriginDbAccess) extends OriginDbAccess {

  private[this] val metrics = Metrics.OriginDbSelectsHistogram

  override def getRowsFromTemplate(fk: ForeignKey, table: Table, fkValue: Any): Vector[Row] = {
    metrics.time(() =>  delegatee.getRowsFromTemplate(fk, table, fkValue))
  }

  override def getRows(query: SqlQuery, table: Table): Vector[Row] = {
    metrics.time(() => delegatee.getRows(query, table))
  }

}