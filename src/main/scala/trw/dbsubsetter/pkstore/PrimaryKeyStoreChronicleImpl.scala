package trw.dbsubsetter.pkstore

import net.openhft.chronicle.core.values.BooleanValue
import net.openhft.chronicle.map.ChronicleMap
import trw.dbsubsetter.db.{PrimaryKeyValue, Table}

import scala.collection.mutable

private[pkstore] final class PrimaryKeyStoreChronicleImpl() extends PrimaryKeyStore {

  /*
   * If `storage(pkValue) == null`, then neither its parents nor its children have been fetched.
   * If `storage(pkValue) == false`, then only its parents have been fetched.
   * If `storage(pkValue) == true`, then both its children and its parents have been fetched.
   * There is no such thing as having fetched a row's children but not having fetched its parents.
   */
  private[this] val storage: mutable.Map[Table, ChronicleMap[Array[Byte], BooleanValue]] = mutable.Map.empty

  override def markSeen(table: Table, primaryKeyValue: PrimaryKeyValue): WriteOutcome = {
    this.synchronized {
      if (!storage.contains(table)) {
        val tableStorage =
          ChronicleMap
            .of(classOf[Array[Byte]], classOf[BooleanValue])
            .name(table.schema + "-" + table.name)
            .entries(10000)
            .averageValue(primaryKeyValue)
            .create
        storage.put()
      }
      val rawValue: Any = PrimaryKeyStoreInMemoryImpl.extract(primaryKeyValue)

      val alreadySeenWithChildren: Boolean =
        seenWithChildrenStorage(table).contains(rawValue)

      // Purposely lazy -- only do this extra work if logically necessary
      lazy val alreadySeenWithoutChildren =
        !storage(table).add(rawValue)

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
        storage(table).remove(rawValue)

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
      seenWithChildrenStorage(table).contains(rawValue) || storage(table).contains(rawValue)
    }
  }
}
