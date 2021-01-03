package trw.dbsubsetter.pkstore

import trw.dbsubsetter.db.PrimaryKeyValue
import trw.dbsubsetter.map.PrimaryKeyMap

/**
  * `!storage.containsKey(pkValue)` means neither its parents nor its children have been fetched.
  * `storage(pkValue) == false` means only its parents have been fetched.
  * `storage(pkValue) == true` means both its children and its parents have been fetched.
  */
private[pkstore] final class PkStoreImpl(storage: PrimaryKeyMap) extends PkStore {

  override def markSeen(pkv: PrimaryKeyValue): WriteOutcome = {
    val prevState: Option[Boolean] = storage.putIfAbsent(pkv, value = false)
    interpret(prevState)
  }

  override def markSeenWithChildren(pkv: PrimaryKeyValue): WriteOutcome = {
    val prev: Option[Boolean] = storage.put(pkv, value = true)
    interpret(prev)
  }

  override def alreadySeen(pkv: PrimaryKeyValue): Boolean = {
    storage.get(pkv).isDefined
  }

  private[this] def interpret(prevState: Option[Boolean]): WriteOutcome = {
    prevState match {
      case None        => FirstTimeSeen
      case Some(false) => AlreadySeenWithoutChildren
      case Some(true)  => AlreadySeenWithChildren
    }
  }
}
