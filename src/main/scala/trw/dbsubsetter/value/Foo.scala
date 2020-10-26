package trw.dbsubsetter.value

import java.nio.ByteBuffer

import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator

object Foo {
  def main(args: Array[String]): Unit = {
    val bb: ByteBuffer = ByteBuffer.allocate(4).putInt(59)
    val ba: Array[Byte] = bb.array()
    val bbSize = ObjectSizeCalculator.getObjectSize(bb)
    val baSize = ObjectSizeCalculator.getObjectSize(ba)
    System.out.println(bbSize)
    System.out.println(baSize)
  }
}
