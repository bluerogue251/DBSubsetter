package trw.dbsubsetter.db.impl.origin

import io.prometheus.client.Histogram
import io.prometheus.client.Histogram.Timer
import trw.dbsubsetter.db.ForeignKey
import trw.dbsubsetter.db.ForeignKeyValue
import trw.dbsubsetter.db.Keys
import trw.dbsubsetter.db.OriginDbAccess
import trw.dbsubsetter.db.PrimaryKeyValue
import trw.dbsubsetter.db.Row
import trw.dbsubsetter.db.SqlQuery
import trw.dbsubsetter.db.Table
import trw.dbsubsetter.metrics.Metrics

private[db] class InstrumentedOriginDbAccess(delegatee: OriginDbAccess) extends OriginDbAccess {

  private[this] val durationPerStatement: Histogram = Metrics.OriginDbDurationPerStatement

  private[this] val rowsFetchedPerStatement: Histogram = Metrics.OriginDbRowsFetchedPerStatement

  private[this] val durationPerRow: Histogram = Metrics.OriginDbDurationPerRow

  override def getRowsFromForeignKeyValue(fk: ForeignKey, table: Table, fkValue: ForeignKeyValue): Vector[Keys] = {
    instrument(() => delegatee.getRowsFromForeignKeyValue(fk, table, fkValue))
  }

  override def getRowsFromPrimaryKeyValues(table: Table, primaryKeyValues: Seq[PrimaryKeyValue]): Vector[Row] = {
    delegatee.getRowsFromPrimaryKeyValues(table, primaryKeyValues)
  }

  override def getRows(query: SqlQuery, table: Table): Vector[Keys] = {
    instrument(() => delegatee.getRows(query, table))
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
