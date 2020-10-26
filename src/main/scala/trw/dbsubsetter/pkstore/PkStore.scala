package trw.dbsubsetter.pkstore

import trw.dbsubsetter.db.PrimaryKeyValue

private[pkstore] trait PkStore {
  def markSeen(primaryKeyValue: PrimaryKeyValue): WriteOutcome
  def markSeenWithChildren(primaryKeyValue: PrimaryKeyValue): WriteOutcome
  def alreadySeen(primaryKeyValue: PrimaryKeyValue): Boolean
}

object PkStore {
  def empty(): PkStore = {
    new PkStoreInMemoryImpl()
  }
}
