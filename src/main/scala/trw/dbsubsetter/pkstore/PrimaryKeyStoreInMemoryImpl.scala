package trw.dbsubsetter.pkstore

import trw.dbsubsetter.db.{PrimaryKeyValue, Table}

import scala.collection.mutable

private[pkstore] final class PrimaryKeyStoreInMemoryImpl(tables: Seq[Table]) extends PrimaryKeyStore {

  /*
   * If `storage(pkValue) == null`, then neither its parents nor its children have been fetched.
   * If `storage(pkValue) == false`, then only its parents have been fetched.
   * If `storage(pkValue) == true`, then both its children and its parents have been fetched.
   * There is no such thing as having fetched a row's children but not having fetched its parents.
   */
  private[this] val storage: Map[Table, mutable.Map[Any, Boolean]] =
    tables
      .map(_ -> collection.mutable.Map[Any, Boolean]())
      .toMap

  override def markSeen(table: Table, primaryKeyValue: PrimaryKeyValue): WriteOutcome = {
    this.synchronized {
      val rawValue: Any = extract(primaryKeyValue)
      val tableStorage: mutable.Map[Any, Boolean] = storage(table)
      val prev: Option[Boolean] = tableStorage.get(rawValue)
      if (prev.isEmpty) {
        tableStorage.put(rawValue, false)
      }
      interpret(prev)
    }
  }

  override def markSeenWithChildren(table: Table, primaryKeyValue: PrimaryKeyValue): WriteOutcome = {
    this.synchronized {
      val rawValue: Any = extract(primaryKeyValue)
      val tableStorage: mutable.Map[Any, Boolean] = storage(table)
      val prev: Option[Boolean] = tableStorage.put(rawValue, true)
      interpret(prev)
    }
  }

  override def alreadySeen(table: Table, primaryKeyValue: PrimaryKeyValue): Boolean = {
    this.synchronized {
      val rawValue: Any = extract(primaryKeyValue)
      val tableStorage: mutable.Map[Any, Boolean] = storage(table)
      tableStorage.contains(rawValue)
    }
  }

  private[this] def extract(primaryKeyValue: PrimaryKeyValue): Any = {
    if (primaryKeyValue.individualColumnValues.size == 1) {
      primaryKeyValue.individualColumnValues.head
    } else {
      primaryKeyValue.individualColumnValues
    }
  }

  private[this] def interpret(prev: Option[Boolean]): WriteOutcome = {
    prev match {
      case None        => FirstTimeSeen
      case Some(false) => AlreadySeenWithoutChildren
      case Some(true)  => AlreadySeenWithChildren
    }
  }
}
