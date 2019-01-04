package trw.dbsubsetter.primarykeystore.impl

import trw.dbsubsetter.db.{SchemaInfo, Table}
import trw.dbsubsetter.primarykeystore.PrimaryKeyStore

import scala.collection.mutable

/*
 * CAREFUL -- NOT THREADSAFE
 */
private[primarykeystore] class InMemoryPrimaryKeyStore(schemaInfo: SchemaInfo) extends PrimaryKeyStore {

  private[this] val tables = schemaInfo.pksByTableOrdered.keys.filter(_.storePks)

  // If `seenWithChildrenStorage` contains a PK, then both its children AND its parents have been fetched.
  // If `seenWithoutChildrenStorage` contains a PK, then only its parents have been fetched.

  // There is no such thing as having fetched a row's children but not having fetched its parents.

  // If a PK is in there at all, then at any given time, it is either in `seenWithoutChildrenStorage` or
  // in `seenWithChildrenStorage` -- it will never be in both at once.
  private[this] val seenWithoutChildrenStorage: Map[Table, mutable.HashSet[Any]] = tables.map { t =>
    t -> mutable.HashSet.empty[Any]
  }.toMap

  private[this] val seenWithChildrenStorage: Map[Table, mutable.HashSet[Any]] = tables.map { t =>
    t -> mutable.HashSet.empty[Any]
  }.toMap

  override def markSeen(table: Table, primaryKeyValue: Any): Boolean = {
    val alreadySeenWithChildren: Boolean =
      seenWithChildrenStorage(table).contains(primaryKeyValue)

    if (alreadySeenWithChildren)
      false
    else
      seenWithoutChildrenStorage(table).add(primaryKeyValue)
  }

  override def markSeenWithChildren(table: Table, primaryKeyValue: Any): Boolean = {
    seenWithoutChildrenStorage(table).remove(primaryKeyValue)
    !seenWithChildrenStorage(table).add(primaryKeyValue)
  }

  override def alreadySeen(table: Table, primaryKeyValue: Any): Boolean = {
    seenWithChildrenStorage(table).contains(primaryKeyValue) ||
      seenWithoutChildrenStorage(table).contains(primaryKeyValue)
  }

  override def alreadySeenWithChildren(table: Table, primaryKeyValue: Any): Boolean = {
    seenWithChildrenStorage(table).contains(primaryKeyValue)
  }
}