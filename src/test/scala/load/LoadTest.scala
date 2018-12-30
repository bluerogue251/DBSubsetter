package load

import e2e.AbstractEndToEndTest
import util.db.Database
import util.runner.TestSubsetRunner


trait LoadTest[T <: Database] extends AbstractEndToEndTest[T] {

  protected def singleThreadedRuntimeLimitMillis: Long

  protected def akkaStreamsRuntimeLimitMillis: Long

  private var singleThreadedRuntimeMillis: Long = _

  private var akkaStreamsRuntimeMillis: Long = _

  override protected def runSubsetInSingleThreadedMode(): Unit = {
    singleThreadedRuntimeMillis = TestSubsetRunner.runSubsetInSingleThreadedMode(containers, programArgs)
  }

  override protected def runSubsetInAkkaStreamsMode(): Unit = {
    akkaStreamsRuntimeMillis = TestSubsetRunner.runSubsetInAkkaStreamsMode(containers, programArgs)
  }

  test("Single threaded runtime did not significantly increase") {
    assert(singleThreadedRuntimeMillis < singleThreadedRuntimeLimitMillis)
  }

  test("Akka Streams runtime did not significantly increase") {
    assert(akkaStreamsRuntimeMillis < akkaStreamsRuntimeLimitMillis)
  }
}
