package trw.dbsubsetter.map

import java.nio.ByteBuffer

import net.openhft.chronicle.map.ChronicleMap

/**
  * Not Threadsafe
  */
private[map] final class StaticMapBytesToBoolChronicleImpl(capacity: Long) extends StaticMap[ByteBuffer, Boolean] {

  private[this] val storage: ChronicleMap[ByteBuffer, Byte] =
    ChronicleMap
      .of(classOf[ByteBuffer], classOf[Byte])
      .entries(capacity)
      .create()

  private[this] var size: Long = 0L

  override def containsKey(key: ByteBuffer): Boolean = {
    storage.containsKey(key)
  }

  override def put(key: ByteBuffer, value: Boolean): Option[Boolean] = {
    val byteValue: Byte = mapToByte(value)
    val prevByte: Byte = storage.put(key, byteValue)
    interpret(prevByte)
  }

  override def putIfAbsent(key: ByteBuffer, value: Boolean): Option[Boolean] = {
    val byteValue: Byte = mapToByte(value)
    val prevByte: Byte = storage.putIfAbsent(key, byteValue)
    interpret(prevByte)
  }

  override def capacity(): Long = {
    capacity
  }

  override def size(): Long = {
    storage.size()
  }

  override def copyTo(other: StaticMap[ByteBuffer, Boolean]): Unit = {
    storage.forEachEntry { entry =>
      val valueBool: Boolean = mapToBoolUnsafe(entry.value().get())
      other.put(entry.key().get(), valueBool)
    }
  }

  override def close(): Unit = {
    storage.close()
  }

  private[this] def mapToByte(bool: Boolean): Byte = {
    if (bool) 1 else 2
  }

  private[this] def interpret(prevByte: Byte): Option[Boolean] = {
    val prevBool: Option[Boolean] = mapToBool(prevByte)
    if (prevBool.isEmpty) {
      size += 1
    }
    prevBool
  }

  private[this] def mapToBool(byte: Byte): Option[Boolean] = {
    byte match {
      case 0 => None
      case 1 => Some(false)
      case 2 => Some(true)
    }
  }

  private[this] def mapToBoolUnsafe(byte: Byte): Boolean = {
    byte match {
      case 1 => false
      case 2 => true
    }
  }
}
