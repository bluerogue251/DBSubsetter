package trw.dbsubsetter.map

import java.nio.ByteBuffer

trait DynamicMapBytesToBool extends DynamicMap[ByteBuffer, Boolean] {}

object DynamicMapBytesToBool {
  def empty(): DynamicMapBytesToBool = {
    new DynamicMapBytesToBoolImpl()
  }
}
