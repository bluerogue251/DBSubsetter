package trw.dbsubsetter.map

import java.nio.ByteBuffer

final class DynamicMapBytesToBoolImpl extends DynamicMapBytesToBool {

  private[this] var storage: StaticMap[ByteBuffer, Boolean] = new StaticMapBytesToBoolInMemoryImpl(5000)

  override def containsKey(key: ByteBuffer): Boolean = {
    this.synchronized {
      storage.containsKey(key)
    }
  }

  override def put(key: ByteBuffer, value: Boolean): Option[Boolean] = {
    this.synchronized {
      manageSize()
      storage.put(key, value)
    }
  }

  override def putIfAbsent(key: ByteBuffer, value: Boolean): Option[Boolean] = {
    this.synchronized {
      manageSize()
      storage.putIfAbsent(key, value)
    }
  }

  private def manageSize(): Unit = {
    if (storage.size() >= storage.capacity()) {
      val newCapacity: Long = storage.capacity() * 2
      val newStorage: StaticMap[ByteBuffer, Boolean] = new StaticMapBytesToBoolChronicleImpl(newCapacity)
      storage.copyTo(newStorage)
      storage.close()
      storage = newStorage
    }
  }
}
