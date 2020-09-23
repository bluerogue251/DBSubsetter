package trw.dbsubsetter.primarykeystore.impl

import trw.dbsubsetter.db.{PrimaryKeyValue, Table}
import trw.dbsubsetter.metrics.Metrics
import trw.dbsubsetter.primarykeystore._

private[primarykeystore] final class InstrumentedPrimaryKeyStore(delegatee: PrimaryKeyStore) extends PrimaryKeyStore {

  private[this] val pkStoreMarkSeenHistogram = Metrics.PkStoreMarkSeenHistogram

  private[this] val pkStoreMarkSeenWithChildrenHistogram = Metrics.PkStoreMarkSeenWithChildrenHistogram

  private[this] val pkStoreQueryAlreadySeenHistogram = Metrics.PkStoreQueryAlreadySeenHistogram

  private[this] val duplicateOriginDbRowsDiscarded = Metrics.DuplicateOriginDbRowsDiscarded

  private[this] val duplicateFkTasksDiscarded = Metrics.DuplicateFkTasksDiscarded

  override def markSeen(table: Table, primaryKeyValue: PrimaryKeyValue): WriteOutcome = {
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

  override def markSeenWithChildren(table: Table, primaryKeyValue: PrimaryKeyValue): WriteOutcome = {
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

  override def alreadySeen(table: Table, primaryKeyValue: PrimaryKeyValue): Boolean = {
    val alreadySeen: Boolean =
      pkStoreQueryAlreadySeenHistogram.time(() => {
        delegatee.alreadySeen(table, primaryKeyValue)
      })

    if (alreadySeen) duplicateFkTasksDiscarded.inc()
    alreadySeen
  }
}
