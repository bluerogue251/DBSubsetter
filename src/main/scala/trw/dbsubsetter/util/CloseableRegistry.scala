package trw.dbsubsetter.util

import scala.collection.mutable

/*
 * CAREFUL: NOT THREADSAFE
 */
class CloseableRegistry {

  private val registry: mutable.Set[Closeable] = mutable.Set.empty[Closeable]

  def register(closeable: Closeable): Unit = {
    registry + closeable
  }

  def closeAll(): Unit = {
    registry.foreach(_.close())
  }
}
