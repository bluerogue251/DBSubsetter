package trw.dbsubsetter.pkstore

import trw.dbsubsetter.db.PrimaryKeyValue
import trw.dbsubsetter.map.BooleanMap

private[pkstore] final class PkStoreImpl extends PkStore {

  /*
   * `!storage.containsKey(pkValue)` means neither its parents nor its children have been fetched.
   * `storage(pkValue) == false` means only its parents have been fetched.
   * `storage(pkValue) == true` means both its children and its parents have been fetched.
   */
  private[this] val storage: BooleanMap[Any] = BooleanMap.empty()

  override def markSeenWithoutChildren(value: PrimaryKeyValue): WriteOutcome = {
    val rawValue: Any = extract(value)
    val prevState: Option[Boolean] = storage.putIfAbsent(rawValue, value = false)
    interpretPrevState(prevState)
  }

  override def markSeenWithChildren(value: PrimaryKeyValue): WriteOutcome = {
    val rawValue: Any = extract(value)
    val prev: Option[Boolean] = storage.put(rawValue, value = true)
    interpretPrevState(prev)
  }

  override def alreadySeen(value: PrimaryKeyValue): Boolean = {
    val rawValue: Any = extract(value)
    storage.get(rawValue)
  }

  private[this] def interpretPrevState(prevState: Option[Boolean]): WriteOutcome = {
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
