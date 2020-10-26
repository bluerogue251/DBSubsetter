package trw.dbsubsetter.pkstore

import trw.dbsubsetter.db.PrimaryKeyValue

import scala.collection.mutable

private[pkstore] final class PkStoreInMemoryImpl extends PkStore {

  /*
   * If `seenWithChildrenStorage` contains a PK, then both its children AND its parents have been fetched.
   * If `seenWithoutChildrenStorage` contains a PK, then only its parents have been fetched
   *
   * There is no such thing as having fetched a row's children but not having fetched its parents. If a PK
   * is in there at all, then at any given time, it is either in `seenWithoutChildrenStorage` or in
   * `seenWithChildrenStorage` -- it will never be in both at once.
   */
  private[this] val seenWithoutChildrenStorage: mutable.HashSet[Any] = mutable.HashSet()

  private[this] val seenWithChildrenStorage: mutable.HashSet[Any] = mutable.HashSet()

  override def markSeen(primaryKeyValue: PrimaryKeyValue): WriteOutcome = {
    this.synchronized {
      val rawValue: Any = extract(primaryKeyValue)

      val alreadySeenWithChildren: Boolean =
        seenWithChildrenStorage.contains(rawValue)

      // Purposely lazy -- only do this extra work if logically necessary
      lazy val alreadySeenWithoutChildren =
        !seenWithoutChildrenStorage.add(rawValue)

      if (alreadySeenWithChildren) {
        AlreadySeenWithChildren
      } else if (alreadySeenWithoutChildren) {
        AlreadySeenWithoutChildren
      } else {
        FirstTimeSeen
      }
    }
  }

  override def markSeenWithChildren(primaryKeyValue: PrimaryKeyValue): WriteOutcome = {
    this.synchronized {
      val rawValue: Any = extract(primaryKeyValue)

      val alreadySeenWithChildren: Boolean =
        !seenWithChildrenStorage.add(rawValue)

      // Purposely lazy -- only do this extra work if logically necessary
      lazy val alreadySeenWithoutChildren: Boolean =
        seenWithoutChildrenStorage.remove(rawValue)

      if (alreadySeenWithChildren) {
        AlreadySeenWithChildren
      } else if (alreadySeenWithoutChildren) {
        AlreadySeenWithoutChildren
      } else {
        FirstTimeSeen
      }
    }
  }

  override def alreadySeen(primaryKeyValue: PrimaryKeyValue): Boolean = {
    this.synchronized {
      val rawValue: Any = extract(primaryKeyValue)
      seenWithChildrenStorage.contains(rawValue) || seenWithoutChildrenStorage.contains(rawValue)
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
