package trw.dbsubsetter.primarykeystore.impl

import trw.dbsubsetter.db.{SchemaInfo, Table}
import trw.dbsubsetter.primarykeystore._

import scala.collection.mutable

/*
 * CAREFUL -- NOT THREADSAFE
 */
private[primarykeystore] final class InMemoryPrimaryKeyStore(schemaInfo: SchemaInfo) extends PrimaryKeyStore {

  /*
   * If `seenWithChildrenStorage` contains a PK, then both its children AND its parents have been fetched.
   * If `seenWithoutChildrenStorage` contains a PK, then only its parents have been fetched
   *
   * There is no such thing as having fetched a row's children but not having fetched its parents. If a PK
   * is in there at all, then at any given time, it is either in `seenWithoutChildrenStorage` or in
   * `seenWithChildrenStorage` -- it will never be in both at once.
   */
  private[this] val seenWithoutChildrenStorage: Map[Table, mutable.HashSet[Any]] =
    InMemoryPrimaryKeyStore.buildStorage(schemaInfo)

  private[this] val seenWithChildrenStorage: Map[Table, mutable.HashSet[Any]] =
    InMemoryPrimaryKeyStore.buildStorage(schemaInfo)

  override def markSeen(table: Table, primaryKeyValue: Any): WriteOutcome = {
    val alreadySeenWithChildren: Boolean =
      seenWithChildrenStorage(table).contains(primaryKeyValue)

    // Purposely lazy -- only do this extra work if logically necessary
    lazy val alreadySeenWithoutChildren =
      !seenWithoutChildrenStorage(table).add(primaryKeyValue)

    if (alreadySeenWithChildren) {
      AlreadySeenWithChildren
    } else if (alreadySeenWithoutChildren) {
      AlreadySeenWithoutChildren
    } else {
      FirstTimeSeen
    }
  }

  override def markSeenWithChildren(table: Table, primaryKeyValue: Any): WriteOutcome = {
    val alreadySeenWithChildren: Boolean =
      !seenWithChildrenStorage(table).add(primaryKeyValue)

    // Purposely lazy -- only do this extra work if logically necessary
    lazy val alreadySeenWithoutChildren: Boolean =
      seenWithoutChildrenStorage(table).remove(primaryKeyValue)

    if (alreadySeenWithChildren)
      AlreadySeenWithChildren
    else if (alreadySeenWithoutChildren)
      AlreadySeenWithoutChildren
    else
      FirstTimeSeen
  }

  override def alreadySeen(table: Table, primaryKeyValue: Any): Boolean = {
    seenWithChildrenStorage(table).contains(primaryKeyValue) ||
      seenWithoutChildrenStorage(table).contains(primaryKeyValue)
  }
}

private object InMemoryPrimaryKeyStore {
  private def buildStorage(schemaInfo: SchemaInfo): Map[Table, mutable.HashSet[Any]] = {
    val tables: Iterable[Table] = schemaInfo.pksByTableOrdered.keys.filter(_.storePks)
    tables.map { t => t -> mutable.HashSet.empty[Any] }.toMap
  }
}