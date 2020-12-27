package trw.dbsubsetter.pkstore

import trw.dbsubsetter.db.PrimaryKeyValue
import trw.dbsubsetter.map.ScalableMap

/**
  * `!storage.containsKey(pkValue)` means neither its parents nor its children have been fetched.
  * `storage(pkValue) == false` means only its parents have been fetched.
  * `storage(pkValue) == true` means both its children and its parents have been fetched.
  */
private[pkstore] final class PkStoreImpl(storage: ScalableMap) extends PkStore {

  override def markSeen(pkValue: PrimaryKeyValue): WriteOutcome = {
    val prevState: Option[Boolean] = storage.putIfAbsent(pkValue.asBytes(), value = false)
    interpret(prevState)
  }

  override def markSeenWithChildren(pkValue: PrimaryKeyValue): WriteOutcome = {
    val prev: Option[Boolean] = storage.put(pkValue.asBytes(), value = true)
    interpret(prev)
  }

  override def alreadySeen(pkValue: PrimaryKeyValue): Boolean = {
    storage.get(pkValue.asBytes()).isDefined
  }

  private[this] def interpret(prevState: Option[Boolean]): WriteOutcome = {
    prevState match {
      case None        => FirstTimeSeen
      case Some(false) => AlreadySeenWithoutChildren
      case Some(true)  => AlreadySeenWithChildren
    }
  }
}
