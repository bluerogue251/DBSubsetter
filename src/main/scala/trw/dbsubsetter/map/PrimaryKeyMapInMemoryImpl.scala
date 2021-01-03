package trw.dbsubsetter.map

import trw.dbsubsetter.db.PrimaryKeyValue

import scala.collection.mutable

/**
  * Invariant: any key at any given moment is in at most one of `falseValues` and `trueValues`; never both.
  */
private[map] final class PrimaryKeyMapInMemoryImpl extends PrimaryKeyMap {

  private[this] val falseValues: mutable.HashSet[PrimaryKeyValue] = mutable.HashSet()

  private[this] val trueValues: mutable.HashSet[PrimaryKeyValue] = mutable.HashSet()

  override def get(key: PrimaryKeyValue): Option[Boolean] = {
    this.synchronized {
      if (falseValues.contains(key)) {
        Some(false)
      } else if (trueValues.contains(key)) {
        Some(true)
      } else {
        None
      }
    }
  }

  override def put(key: PrimaryKeyValue, value: Boolean): Option[Boolean] = {
    this.synchronized {
      if (value) {
        val wasFalse = falseValues.remove(key)
        val wasTrue = !trueValues.add(key)
        massage(wasFalse, wasTrue)
      } else {
        val wasTrue = trueValues.remove(key)
        val wasFalse = !falseValues.add(key)
        massage(wasFalse, wasTrue)
      }
    }
  }

  override def putIfAbsent(key: PrimaryKeyValue, value: Boolean): Option[Boolean] = {
    this.synchronized {
      val existingValue: Option[Boolean] = get(key)
      if (existingValue.isDefined) {
        existingValue
      } else if (value) {
        trueValues.add(key)
        None
      } else {
        falseValues.add(key)
        None
      }
    }
  }

  private[this] def massage(wasFalse: Boolean, wasTrue: Boolean): Option[Boolean] = {
    if (wasFalse) {
      Some(false)
    } else if (wasTrue) {
      Some(true)
    } else {
      None
    }
  }
}