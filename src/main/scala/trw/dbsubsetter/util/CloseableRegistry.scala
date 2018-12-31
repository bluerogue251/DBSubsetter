package trw.dbsubsetter.util

import scala.collection.mutable

/*
 * CAREFUL: NOT THREADSAFE
 */
class CloseableRegistry {

  /*
   * Records all open connections so that we can remember to call `close()` on them when we are finished
   */
  private val registry: mutable.Set[Closeable] = mutable.Set.empty[Closeable]

  def register(closeable: Closeable): Unit = {
    registry.add(closeable)
  }

  def closeAll(): Unit = {
    registry.foreach(_.close())
  }
}
