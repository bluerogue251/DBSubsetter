package load

import org.scalatest.FunSuite


trait LoadTest extends FunSuite {
  def singleThreadedRuntimeMillis: Long

  def akkStreamsRuntimeMillis: Long

  def singleThreadedRuntimeThreshold: Long

  def akkaStreamsRuntimeThreshold: Long

  test("Single threaded Runtime did not significantly increase") {
    assert(singleThreadedRuntimeMillis < singleThreadedRuntimeThreshold)
  }

  test("Akka Streams runtime did not significantly increase") {
    assert(akkStreamsRuntimeMillis < akkaStreamsRuntimeThreshold)
  }
}
