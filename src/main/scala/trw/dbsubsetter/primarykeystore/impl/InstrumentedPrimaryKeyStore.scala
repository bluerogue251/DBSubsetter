package trw.dbsubsetter.primarykeystore.impl

import trw.dbsubsetter.db.Table
import trw.dbsubsetter.metrics.Metrics
import trw.dbsubsetter.primarykeystore._


private[primarykeystore] final class InstrumentedPrimaryKeyStore(delegatee: PrimaryKeyStore) extends PrimaryKeyStore {

  private[this] val duplicateOriginDbRowsDiscarded = Metrics.DuplicateOriginDbRowsDiscarded

  private[this] val duplicateFkTasksDiscarded = Metrics.DuplicateFkTasksDiscarded

  override def markSeen(table: Table, primaryKeyValue: Any): WriteOutcome = {
    val writeOutcome: WriteOutcome =
      delegatee.markSeen(table, primaryKeyValue)

    writeOutcome match {
      case FirstTimeSeen =>
      case _ => duplicateOriginDbRowsDiscarded.inc()
    }

    writeOutcome
  }

  override def markSeenWithChildren(table: Table, primaryKeyValue: Any): WriteOutcome = {
    val writeOutcome: WriteOutcome =
      delegatee.markSeenWithChildren(table, primaryKeyValue)

    writeOutcome match {
      case FirstTimeSeen =>
      case _ => duplicateOriginDbRowsDiscarded.inc()
    }

    writeOutcome
  }

  override def alreadySeen(table: Table, primaryKeyValue: Any): Boolean = {
    val alreadySeen: Boolean = delegatee.alreadySeen(table, primaryKeyValue)
    if (alreadySeen) duplicateFkTasksDiscarded.inc()
    alreadySeen
  }
}

