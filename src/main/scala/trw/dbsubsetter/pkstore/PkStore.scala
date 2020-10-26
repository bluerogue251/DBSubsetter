package trw.dbsubsetter.pkstore

import trw.dbsubsetter.db.PrimaryKeyValue

private[pkstore] trait PkStore {
  def markSeen(value: PrimaryKeyValue): WriteOutcome
  def markSeenWithChildren(value: PrimaryKeyValue): WriteOutcome
  def alreadySeen(value: PrimaryKeyValue): Boolean
}

object PkStore {
  def empty(): PkStore = {
    new PkStoreImpl()
  }
}
