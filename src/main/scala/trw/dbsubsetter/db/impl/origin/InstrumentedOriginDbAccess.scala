package trw.dbsubsetter.db.impl.origin

import io.prometheus.client.Histogram
import io.prometheus.client.Histogram.Timer
import trw.dbsubsetter.db.{ForeignKey, ForeignKeyValue, OriginDbAccess, PrimaryKeyValue, Row, SqlQuery, Table}
import trw.dbsubsetter.metrics.Metrics

private[db] class InstrumentedOriginDbAccess(delegatee: OriginDbAccess) extends OriginDbAccess {

  private[this] val durationPerStatement: Histogram = Metrics.OriginDbDurationPerStatement

  private[this] val rowsFetchedPerStatement: Histogram = Metrics.OriginDbRowsFetchedPerStatement

  private[this] val durationPerRow: Histogram = Metrics.OriginDbDurationPerRow

  override def getRowsFromForeignKeyValue(fk: ForeignKey, table: Table, fkValue: ForeignKeyValue): Vector[Row] = {
    instrument(() => delegatee.getRowsFromForeignKeyValue(fk, table, fkValue))
  }

  override def getRowsFromPrimaryKeyValues(table: Table, primaryKeyValues: Seq[PrimaryKeyValue]): Vector[Row] = {
    delegatee.getRowsFromPrimaryKeyValues(table, primaryKeyValues)
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
