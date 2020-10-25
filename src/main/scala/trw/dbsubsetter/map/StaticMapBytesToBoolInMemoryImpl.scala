package trw.dbsubsetter.map

import java.nio.ByteBuffer

import scala.collection.mutable

/**
  * Not Threadsafe
  */
private[map] final class StaticMapBytesToBoolInMemoryImpl(capacity: Int) extends StaticMap[ByteBuffer, Boolean] {

  private[this] val storage: mutable.Map[ByteBuffer, Boolean] = mutable.Map()

  override def containsKey(key: ByteBuffer): Boolean = {
    storage.contains(key)
  }

  override def put(key: ByteBuffer, value: Boolean): Option[Boolean] = {
    storage.put(key, value)
  }

  override def putIfAbsent(key: ByteBuffer, value: Boolean): Option[Boolean] = {
    val existing: Option[Boolean] = storage.get(key)
    if (existing.isDefined) {
      existing
    } else {
      storage.put(key, value)
    }
  }

  override def capacity(): Long = {
    capacity
  }

  override def size(): Long = {
    storage.size
  }

  override def keySample(): ByteBuffer = {
    storage.keys.head
  }

  override def copyTo(other: StaticMap[ByteBuffer, Boolean]): Unit = {
    storage.foreach { case (key, value) =>
      other.put(key, value)
    }
  }

  override def close(): Unit = {
    // No-Op
  }
}
