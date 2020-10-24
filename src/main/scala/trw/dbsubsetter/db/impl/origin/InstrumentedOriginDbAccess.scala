package trw.dbsubsetter.db.impl.origin

import io.prometheus.client.Histogram
import io.prometheus.client.Histogram.Timer
import trw.dbsubsetter.db.{ForeignKey, ForeignKeyValue, Keys, MultiColumnPrimaryKeyValue, OriginDbAccess, Row, Table}
import trw.dbsubsetter.metrics.Metrics

private[db] class InstrumentedOriginDbAccess(delegatee: OriginDbAccess) extends OriginDbAccess {

  private[this] val durationPerStatement: Histogram = Metrics.OriginDbDurationPerStatement

  private[this] val rowsFetchedPerStatement: Histogram = Metrics.OriginDbRowsFetchedPerStatement

  private[this] val durationPerRow: Histogram = Metrics.OriginDbDurationPerRow

  override def getRowsFromForeignKeyValue(fk: ForeignKey, table: Table, fkValue: ForeignKeyValue): Vector[Keys] = {
    instrument(() => delegatee.getRowsFromForeignKeyValue(fk, table, fkValue))
  }

  override def getRowsFromPrimaryKeyValues(
      table: Table,
      primaryKeyValues: Seq[MultiColumnPrimaryKeyValue]
  ): Vector[Row] = {
    delegatee.getRowsFromPrimaryKeyValues(table, primaryKeyValues)
  }

  override def getRowsFromWhereClause(table: Table, whereClause: String): Vector[Keys] = {
    instrument(() => delegatee.getRowsFromWhereClause(table, whereClause))
  }

  private[this] def instrument(func: () => Vector[Keys]): Vector[Keys] = {
    val timer: Timer = durationPerStatement.startTimer()
    val result: Vector[Keys] = func.apply()
    val statementDuration: Double = timer.observeDuration()
    rowsFetchedPerStatement.observe(result.length)
    if (result.nonEmpty) { // TODO test the divide by zero case
      durationPerRow.observe(statementDuration / result.length)
    }
    result
  }
}
