package trw.dbsubsetter.util

object Util {
  def printRuntime(start: Long): Unit = {
    val end = System.nanoTime()
    val tookSeconds = (end - start) / 1000000000
    println(s"DBSubsetter has completed successfully! Approximate runtime: $tookSeconds seconds")
  }
}
