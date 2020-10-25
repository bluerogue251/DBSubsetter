package trw.dbsubsetter.pkstore

import trw.dbsubsetter.db.PrimaryKeyValue

private[pkstore] trait PrimaryKeyStoreSingleTable {
  def markSeen(value: PrimaryKeyValue): WriteOutcome
  def markSeenWithChildren(value: PrimaryKeyValue): WriteOutcome
  def alreadySeen(value: PrimaryKeyValue): Boolean
}
