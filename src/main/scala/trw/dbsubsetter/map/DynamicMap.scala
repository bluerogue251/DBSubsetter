package trw.dbsubsetter.map

/**
  * A threadsafe map which resizes automatically as the number of mappings it manages grows, eventually switching to
  * off-heap storage if its size becomes too large to fit comfortably in memory
  */
trait DynamicMap[K, V] {

  /**
    * @return `true` if the map contained the key, `false` if it did not
    */
  def containsKey(key: K): Boolean

  /**
    * Set the value for this key, overwriting any previous value
    *
    * @return The previous value, or None if there was no previous value
    */
  def put(key: K, value: V): Option[V]

  /**
    * Add the value to the map, or no-op if there was a previously mapped value for the key
    *
    * @return The previous value, or None if there was no previous value
    */
  def putIfAbsent(key: K, value: V): Option[V]

}
