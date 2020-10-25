package trw.dbsubsetter.map

/**
  * A map which is not necessarily threadsafe and which does not necessarily resize automatically as the number of
  * mappings it manages grows.
  */
private[map] trait StaticMap[K, V] {

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

  /**
    * @return The maximum number of entries this map supports before its behavior becomes undefined
    */
  def capacity(): Long

  /**
    * @return The current number of entries stored in this map
    */
  def size(): Long

  /**
    * Copy every entry in this map into another map
    */
  def copyTo(other: StaticMap[K, V]): Unit

  /**
    * Release any resources held by this map
    */
  def close(): Unit

}
