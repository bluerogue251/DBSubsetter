package trw.dbsubsetter.pkstore

import trw.dbsubsetter.db.{PrimaryKeyValue, Table}
import trw.dbsubsetter.metrics.Metrics

private[pkstore] final class MultiTablePkStoreInstrumentedImpl(delegatee: MultiTablePkStore) extends MultiTablePkStore {

  private[this] val pkStoreMarkSeenHistogram = Metrics.PkStoreMarkSeenHistogram

  private[this] val pkStoreMarkSeenWithChildrenHistogram = Metrics.PkStoreMarkSeenWithChildrenHistogram

  private[this] val pkStoreQueryAlreadySeenHistogram = Metrics.PkStoreQueryAlreadySeenHistogram

  private[this] val duplicateOriginDbRowsDiscarded = Metrics.DuplicateOriginDbRowsDiscarded

  private[this] val duplicateFkTasksDiscarded = Metrics.DuplicateFkTasksDiscarded

  override def markSeen(table: Table, value: PrimaryKeyValue): WriteOutcome = {
    val writeOutcome: WriteOutcome =
      pkStoreMarkSeenHistogram.time(() => {
        delegatee.markSeen(table, value)
      })

    writeOutcome match {
      case FirstTimeSeen =>
      case _             => duplicateOriginDbRowsDiscarded.inc()
    }

    writeOutcome
  }

  override def markSeenWithChildren(table: Table, value: PrimaryKeyValue): WriteOutcome = {
    val writeOutcome: WriteOutcome =
      pkStoreMarkSeenWithChildrenHistogram.time(() => {
        delegatee.markSeenWithChildren(table, value)
      })

    writeOutcome match {
      case FirstTimeSeen =>
      case _             => duplicateOriginDbRowsDiscarded.inc()
    }

    writeOutcome
  }

  override def alreadySeen(table: Table, value: PrimaryKeyValue): Boolean = {
    val alreadySeen: Boolean =
      pkStoreQueryAlreadySeenHistogram.time(() => {
        delegatee.alreadySeen(table, value)
      })

    if (alreadySeen) duplicateFkTasksDiscarded.inc()
    alreadySeen
  }
}
