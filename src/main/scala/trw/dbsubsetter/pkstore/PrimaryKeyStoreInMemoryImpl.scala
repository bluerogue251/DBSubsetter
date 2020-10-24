package trw.dbsubsetter.pkstore

import trw.dbsubsetter.db
import trw.dbsubsetter.db.{MultiColumnPrimaryKeyValue, PrimaryKeyValue, Table}

import scala.collection.mutable

private[pkstore] final class PrimaryKeyStoreInMemoryImpl(tables: Seq[Table]) extends PrimaryKeyStore {

  /*
   * If `seenWithChildrenStorage` contains a PK, then both its children AND its parents have been fetched.
   * If `seenWithoutChildrenStorage` contains a PK, then only its parents have been fetched
   *
   * There is no such thing as having fetched a row's children but not having fetched its parents. If a PK
   * is in there at all, then at any given time, it is either in `seenWithoutChildrenStorage` or in
   * `seenWithChildrenStorage` -- it will never be in both at once.
   */
  private[this] val seenWithoutChildrenStorage: Map[Table, mutable.HashSet[Any]] =
    PrimaryKeyStoreInMemoryImpl.buildStorage(tables)

  private[this] val seenWithChildrenStorage: Map[Table, mutable.HashSet[Any]] =
    PrimaryKeyStoreInMemoryImpl.buildStorage(tables)

  override def markSeen(table: Table, primaryKeyValue: PrimaryKeyValue): WriteOutcome = {
    this.synchronized {
      val rawValue: Any = PrimaryKeyStoreInMemoryImpl.extract(primaryKeyValue)

      val alreadySeenWithChildren: Boolean =
        seenWithChildrenStorage(table).contains(rawValue)

      // Purposely lazy -- only do this extra work if logically necessary
      lazy val alreadySeenWithoutChildren =
        !seenWithoutChildrenStorage(table).add(rawValue)

      if (alreadySeenWithChildren) {
        AlreadySeenWithChildren
      } else if (alreadySeenWithoutChildren) {
        AlreadySeenWithoutChildren
      } else {
        FirstTimeSeen
      }
    }
  }

  override def markSeenWithChildren(table: Table, primaryKeyValue: PrimaryKeyValue): WriteOutcome = {
    this.synchronized {
      val rawValue: Any = PrimaryKeyStoreInMemoryImpl.extract(primaryKeyValue)

      val alreadySeenWithChildren: Boolean =
        !seenWithChildrenStorage(table).add(rawValue)

      // Purposely lazy -- only do this extra work if logically necessary
      lazy val alreadySeenWithoutChildren: Boolean =
        seenWithoutChildrenStorage(table).remove(rawValue)

      if (alreadySeenWithChildren) {
        AlreadySeenWithChildren
      } else if (alreadySeenWithoutChildren) {
        AlreadySeenWithoutChildren
      } else {
        FirstTimeSeen
      }
    }
  }

  override def alreadySeen(table: Table, primaryKeyValue: PrimaryKeyValue): Boolean = {
    this.synchronized {
      val rawValue: Any = PrimaryKeyStoreInMemoryImpl.extract(primaryKeyValue)
      seenWithChildrenStorage(table).contains(rawValue) || seenWithoutChildrenStorage(table).contains(rawValue)
    }
  }
}

private object PrimaryKeyStoreInMemoryImpl {
  private def buildStorage(tables: Seq[Table]): Map[Table, mutable.HashSet[Any]] = {
    tables.map { t => t -> mutable.HashSet.empty[Any] }.toMap
  }

  private def extract(primaryKeyValue: PrimaryKeyValue): Any = {
    primaryKeyValue match {
      case db.SingleColumnPrimaryKeyValue(value) => value
      case MultiColumnPrimaryKeyValue(values)    => values
    }
  }
}
