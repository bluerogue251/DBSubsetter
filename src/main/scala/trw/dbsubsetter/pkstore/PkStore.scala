package trw.dbsubsetter.pkstore

import trw.dbsubsetter.db.PrimaryKeyValue
import trw.dbsubsetter.map.BooleanMap

private[pkstore] trait PkStore {
  def markSeen(value: PrimaryKeyValue): WriteOutcome
  def markSeenWithChildren(value: PrimaryKeyValue): WriteOutcome
  def alreadySeen(value: PrimaryKeyValue): Boolean
}

object PkStore {
  def empty(): PkStore = {
    val storage: BooleanMap[Any] = BooleanMap.empty()
    new PkStoreImpl(storage)
  }
}
