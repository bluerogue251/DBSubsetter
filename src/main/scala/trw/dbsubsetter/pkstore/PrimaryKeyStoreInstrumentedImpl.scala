package trw.dbsubsetter.pkstore

import trw.dbsubsetter.db.{MultiColumnPrimaryKeyValue, Table}
import trw.dbsubsetter.metrics.Metrics

private[pkstore] final class PrimaryKeyStoreInstrumentedImpl(delegatee: PrimaryKeyStore) extends PrimaryKeyStore {

  private[this] val pkStoreMarkSeenHistogram = Metrics.PkStoreMarkSeenHistogram

  private[this] val pkStoreMarkSeenWithChildrenHistogram = Metrics.PkStoreMarkSeenWithChildrenHistogram

  private[this] val pkStoreQueryAlreadySeenHistogram = Metrics.PkStoreQueryAlreadySeenHistogram

  private[this] val duplicateOriginDbRowsDiscarded = Metrics.DuplicateOriginDbRowsDiscarded

  private[this] val duplicateFkTasksDiscarded = Metrics.DuplicateFkTasksDiscarded

  override def markSeen(table: Table, primaryKeyValue: MultiColumnPrimaryKeyValue): WriteOutcome = {
    val writeOutcome: WriteOutcome =
      pkStoreMarkSeenHistogram.time(() => {
        delegatee.markSeen(table, primaryKeyValue)
      })

    writeOutcome match {
      case FirstTimeSeen =>
      case _             => duplicateOriginDbRowsDiscarded.inc()
    }

    writeOutcome
  }

  override def markSeenWithChildren(table: Table, primaryKeyValue: MultiColumnPrimaryKeyValue): WriteOutcome = {
    val writeOutcome: WriteOutcome =
      pkStoreMarkSeenWithChildrenHistogram.time(() => {
        delegatee.markSeenWithChildren(table, primaryKeyValue)
      })

    writeOutcome match {
      case FirstTimeSeen =>
      case _             => duplicateOriginDbRowsDiscarded.inc()
    }

    writeOutcome
  }

  override def alreadySeen(table: Table, primaryKeyValue: MultiColumnPrimaryKeyValue): Boolean = {
    val alreadySeen: Boolean =
      pkStoreQueryAlreadySeenHistogram.time(() => {
        delegatee.alreadySeen(table, primaryKeyValue)
      })

    if (alreadySeen) duplicateFkTasksDiscarded.inc()
    alreadySeen
  }
}
