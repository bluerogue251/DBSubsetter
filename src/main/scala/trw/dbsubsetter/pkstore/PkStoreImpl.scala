package trw.dbsubsetter.pkstore

import trw.dbsubsetter.db.PrimaryKeyValue
import trw.dbsubsetter.map.BooleanMap

/**
  * `!storage.containsKey(pkValue)` means neither its parents nor its children have been fetched.
  * `storage(pkValue) == false` means only its parents have been fetched.
  * `storage(pkValue) == true` means both its children and its parents have been fetched.
  */
private[pkstore] final class PkStoreImpl(storage: BooleanMap[Any]) extends PkStore {

  override def markSeen(value: PrimaryKeyValue): WriteOutcome = {
    val rawValue: Any = extract(value)
    val prevState: Option[Boolean] = storage.putIfAbsent(rawValue, value = false)
    interpret(prevState)
  }

  override def markSeenWithChildren(value: PrimaryKeyValue): WriteOutcome = {
    val rawValue: Any = extract(value)
    val prev: Option[Boolean] = storage.put(rawValue, value = true)
    interpret(prev)
  }

  override def alreadySeen(value: PrimaryKeyValue): Boolean = {
    val rawValue: Any = extract(value)
    storage.get(rawValue).isDefined
  }

  private[this] def interpret(prevState: Option[Boolean]): WriteOutcome = {
    prevState match {
      case None        => FirstTimeSeen
      case Some(false) => AlreadySeenWithoutChildren
      case Some(true)  => AlreadySeenWithChildren
    }
  }

  private[this] def extract(primaryKeyValue: PrimaryKeyValue): Any = {
    if (primaryKeyValue.individualColumnValues.size == 1) {
      primaryKeyValue.individualColumnValues.head
    } else {
      primaryKeyValue.individualColumnValues
    }
  }
}
