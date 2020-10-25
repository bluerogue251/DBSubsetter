package trw.dbsubsetter.map

import java.nio.ByteBuffer

import net.openhft.chronicle.map.ChronicleMap

/**
  * Not Threadsafe
  */
private[map] final class StaticMapBytesToBoolChronicleImpl(capacity: Long, keySample: ByteBuffer)
    extends StaticMap[ByteBuffer, Boolean] {

  private[this] val storage: ChronicleMap[ByteBuffer, java.lang.Boolean] =
    ChronicleMap
      .of(classOf[ByteBuffer], classOf[java.lang.Boolean])
      .entries(capacity)
      .averageKey(keySample)
      .create()

  private[this] var size: Long = 0L

  override def containsKey(key: ByteBuffer): Boolean = {
    storage.containsKey(key)
  }

  override def put(key: ByteBuffer, value: Boolean): Option[Boolean] = {
    val prev: java.lang.Boolean = storage.put(key, value)
    interpretAndTrackSize(prev)
  }

  override def putIfAbsent(key: ByteBuffer, value: Boolean): Option[Boolean] = {
    val prev: java.lang.Boolean = storage.putIfAbsent(key, value)
    interpretAndTrackSize(prev)
  }

  override def capacity(): Long = {
    capacity
  }

  override def size(): Long = {
    storage.size()
  }

  override def keySample(): ByteBuffer = {
    storage.keySet().iterator().next()
  }

  override def copyTo(other: StaticMap[ByteBuffer, Boolean]): Unit = {
    storage.forEachEntry { entry =>
      other.put(entry.key().get(), entry.value().get())
    }
  }

  override def close(): Unit = {
    storage.close()
  }

  private[this] def interpretAndTrackSize(prev: java.lang.Boolean): Option[Boolean] = {
    if (prev == null) {
      size += 1
      None
    } else {
      Some(prev.booleanValue())
    }
  }
}
