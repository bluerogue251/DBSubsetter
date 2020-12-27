package trw.dbsubsetter.map

import java.nio.ByteBuffer

/**
  * A mutable, threadsafe map. Keys are byte buffers, values are booleans.
  */
trait ScalableMap {

  /**
    * @return The current value, or None if the key is not in the map
    */
  def get(key: ByteBuffer): Option[Boolean]

  /**
    * Set the value for this key, overwriting any previous value
    *
    * @return The previous value, or None if the key was not previously in the map
    */
  def put(key: ByteBuffer, value: Boolean): Option[Boolean]

  /**
    * Add the value to the map, but only if the key does not already exist in the map. A return value of None
    * indicates the operation went through successfully. A return value of Some(value) indicates a no-op.
    *
    * @return The previously existing value, or None if the key was not previously in the map
    */
  def putIfAbsent(key: ByteBuffer, value: Boolean): Option[Boolean]

}

object ScalableMap {
  def empty(): ScalableMap = {
    new ScalableMapInMemoryImpl()
  }
}
