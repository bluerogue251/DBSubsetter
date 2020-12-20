package trw.dbsubsetter.map

import org.rocksdb.{Options, RocksDB}

import java.io.File
import java.nio.ByteBuffer
import java.nio.file.Files
import java.util.UUID

private[map] final class BooleanMapRocksDbImpl[K]() extends BooleanMap[K] {
  RocksDB.loadLibrary()
  private val options: Options = new Options().setCreateIfMissing(true)
  private val dbDir: File = new File("/tmp/" + "rocks-db-" + UUID.randomUUID().toString)
  Files.createDirectory(dbDir.getAbsoluteFile.toPath)
  private val db: RocksDB = RocksDB.open(options, dbDir.getAbsolutePath)

  private val falseBytes: Array[Byte] = Array[Byte](0)
  private val trueBytes: Array[Byte] = Array[Byte](1)

  override def get(key: K): Option[Boolean] = {
    this.synchronized {
      val keyBytes: Array[Byte] = extract(key).array()
      val valueBytes: Array[Byte] = db.get(keyBytes)
      massage(valueBytes)
    }
  }

  override def put(key: K, value: Boolean): Option[Boolean] = {
    this.synchronized {
      val prev: Option[Boolean] = get(key)
      val keyBytes: Array[Byte] = extract(key).array()
      if (value) {
        db.put(keyBytes, trueBytes)
      } else {
        db.put(keyBytes, falseBytes)
      }
      prev
    }
  }

  override def putIfAbsent(key: K, value: Boolean): Option[Boolean] = {
    this.synchronized {
      val prev: Option[Boolean] = get(key)
      if (prev.isDefined) {
        prev
      } else {
        put(key, value)
      }
    }
  }

  private[this] def massage(valueBytes: Array[Byte]): Option[Boolean] = {
    if (valueBytes == null) {
      None
    } else if (valueBytes(0) == 0) {
      Some(false)
    } else {
      Some(true)
    }
  }

  private def extract(value: Any): ByteBuffer = {
    if (!value.isInstanceOf[Seq[Any]]) {
      extractSingle(value)
    } else {
      val seqValue: Seq[Any] = value.asInstanceOf[Seq[Any]]
      val buffers: Seq[ByteBuffer] = seqValue.map(extractSingle)
      val count: Int = buffers.size
      val bufferSizes: Seq[Int] = buffers.map(_.capacity())
      /*
       * 4 Bytes for the count
       * 4 Bytes per buffer for its size
       * Rest Bytes for all the contents
       */
      val capacity: Int = 4 + (4 * count) + bufferSizes.sum
      val compositeBuffer: ByteBuffer = ByteBuffer.allocate(capacity)
      compositeBuffer.putInt(count)
      bufferSizes.foreach(compositeBuffer.putInt)
      buffers.foreach(compositeBuffer.put)
      compositeBuffer.rewind()
      compositeBuffer
    }
  }

  private def extractSingle(value: Any): ByteBuffer = {
    val buffer: ByteBuffer = {
      value match {
        case short: Short                     => ByteBuffer.allocate(2).putShort(short)
        case int: Int                         => ByteBuffer.allocate(4).putInt(int)
        case long: Long                       => ByteBuffer.allocate(8).putLong(long)
        case bigInt: BigInt                   => ByteBuffer.wrap(bigInt.toByteArray)
        case bigInteger: java.math.BigInteger => ByteBuffer.wrap(bigInteger.toByteArray)
        case string: String                   => ByteBuffer.wrap(string.getBytes)
        case bytes: Array[Byte]               => ByteBuffer.wrap(bytes)
        case uuid: UUID =>
          val buffer: ByteBuffer = ByteBuffer.allocate(16)
          buffer.putLong(uuid.getMostSignificantBits)
          buffer.putLong(uuid.getMostSignificantBits)
          buffer
      }
    }
    buffer.rewind()
    buffer
  }
}
