package trw.dbsubsetter.pkstore

import trw.dbsubsetter.db.PrimaryKeyValue
import trw.dbsubsetter.map.PrimaryKeyMap

private[pkstore] trait PkStore {
  def markSeen(value: PrimaryKeyValue): WriteOutcome
  def markSeenWithChildren(value: PrimaryKeyValue): WriteOutcome
  def alreadySeen(value: PrimaryKeyValue): Boolean
}

object PkStore {
  def empty(): PkStore = {
    val storage: PrimaryKeyMap = PrimaryKeyMap.empty()
    new PkStoreImpl(storage)
  }
}
