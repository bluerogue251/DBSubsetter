package trw.dbsubsetter.pkstore

import java.util.concurrent.ConcurrentHashMap

import trw.dbsubsetter.db.{PrimaryKeyValue, Table}

private[pkstore] final class PrimaryKeyStoreInMemoryImpl(val tables: Seq[Table]) extends PrimaryKeyStore {

  /*
   * If `storage(pkValue) == null`, then neither its parents nor its children have been fetched.
   * If `storage(pkValue) == false`, then only its parents have been fetched.
   * If `storage(pkValue) == true`, then both its children and its parents have been fetched.
   * There is no such thing as having fetched a row's children but not having fetched its parents.
   */
  private[this] val storage: Map[Table, ConcurrentHashMap[Any, java.lang.Boolean]] =
    tables
      .map(_ -> new ConcurrentHashMap[Any, java.lang.Boolean])
      .toMap

  override def markSeen(table: Table, primaryKeyValue: PrimaryKeyValue): WriteOutcome = {
    val rawValue: Any = extract(primaryKeyValue)
    val tableStorage: ConcurrentHashMap[Any, java.lang.Boolean] = storage(table)
    val prev: java.lang.Boolean = tableStorage.putIfAbsent(rawValue, false)
    interpret(prev)
  }

  override def markSeenWithChildren(table: Table, primaryKeyValue: PrimaryKeyValue): WriteOutcome = {
    val rawValue: Any = extract(primaryKeyValue)
    val tableStorage: ConcurrentHashMap[Any, java.lang.Boolean] = storage(table)
    val prev: java.lang.Boolean = tableStorage.put(rawValue, true)
    interpret(prev)
  }

  override def alreadySeen(table: Table, primaryKeyValue: PrimaryKeyValue): Boolean = {
    val rawValue: Any = extract(primaryKeyValue)
    val tableStorage: ConcurrentHashMap[Any, java.lang.Boolean] = storage(table)
    tableStorage.containsKey(rawValue)
  }

  private[this] def interpret(previousValue: java.lang.Boolean): WriteOutcome = {
    if (previousValue == null) {
      FirstTimeSeen
    } else if (previousValue) {
      AlreadySeenWithChildren
    } else {
      AlreadySeenWithoutChildren
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
