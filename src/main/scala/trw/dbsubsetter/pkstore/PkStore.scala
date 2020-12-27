package trw.dbsubsetter.pkstore

import trw.dbsubsetter.db.PrimaryKeyValue
import trw.dbsubsetter.map.ScalableMap

private[pkstore] trait PkStore {
  def markSeen(pkValue: PrimaryKeyValue): WriteOutcome
  def markSeenWithChildren(pkValue: PrimaryKeyValue): WriteOutcome
  def alreadySeen(pkValue: PrimaryKeyValue): Boolean
}

object PkStore {
  def empty(): PkStore = {
    val storage: ScalableMap = ScalableMap.empty()
    new PkStoreImpl(storage)
  }
}
