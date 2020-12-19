package trw.dbsubsetter.map

/**
  * A mutable, threadsafe map. Keys may be of any type. Values are restricted to Booleans.
  */
trait BooleanMap[K] {

  /**
    * @return The current value, or None if there is no current value
    */
  def get(key: K): Option[Boolean]

  /**
    * Set the value for this key, overwriting any previous value
    *
    * @return The previous value, or None if there was no previous value
    */
  def put(key: K, value: Boolean): Option[Boolean]

  /**
    * Add the value to the map, or no-op if there was a previously mapped value for the key
    *
    * @return The previous value, or None if there was no previous value
    */
  def putIfAbsent(key: K, value: Boolean): Option[Boolean]

}

object BooleanMap {
  def empty[K](): BooleanMap[K] = {
    new BooleanMapInMemoryImpl[K]()
  }
}
