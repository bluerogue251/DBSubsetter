package trw.dbsubsetter.map

import java.nio.ByteBuffer

private[map] final class DynamicMapBytesToBoolImpl extends DynamicMap[ByteBuffer, Boolean] {

  private[this] var storage: StaticMap[ByteBuffer, Boolean] = new StaticMapBytesToBoolInMemoryImpl(25000)

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
      val keySample: ByteBuffer = storage.keySample()
      val newStorage: StaticMap[ByteBuffer, Boolean] = new StaticMapBytesToBoolChronicleImpl(newCapacity, keySample)
      storage.copyTo(newStorage)
      storage.close()
      storage = newStorage
    }
  }
}
