package trw.dbsubsetter.map

import java.nio.ByteBuffer

import net.openhft.chronicle.map.ChronicleMap

/**
  * Not Threadsafe
  */
private[map] final class StaticMapBytesToBoolChronicleImpl(capacity: Long) extends StaticMap[ByteBuffer, Boolean] {

  private[this] val storage: ChronicleMap[ByteBuffer, Boolean] =
    ChronicleMap
      .of(classOf[ByteBuffer], classOf[Boolean])
      .entries(capacity)
      .create()

  private[this] var size: Long = 0L

  override def containsKey(key: ByteBuffer): Boolean = {
    storage.containsKey(key)
  }

  override def put(key: ByteBuffer, value: Boolean): Option[Boolean] = {
    if (storage.containsKey(key)) {
      val prev: Boolean = storage.put(key, value)
      Some(prev)
    } else {
      size += 1
      storage.put(key, value)
      None
    }
  }

  override def putIfAbsent(key: ByteBuffer, value: Boolean): Option[Boolean] = {
    if (storage.containsKey(key)) {
      val prev: Boolean = storage.get(key)
      Some(prev)
    } else {
      size += 1
      storage.put(key, value)
      Some(value)
    }
  }

  override def capacity(): Long = {
    capacity
  }

  override def size(): Long = {
    storage.size()
  }

  override def copyTo(other: StaticMap[ByteBuffer, Boolean]): Unit = {
    storage.forEachEntry { entry =>
      other.put(entry.key().get(), entry.value().get())
    }
  }

  override def close(): Unit = {
    storage.close()
  }
}
