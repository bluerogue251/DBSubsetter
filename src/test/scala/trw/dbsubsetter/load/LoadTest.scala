package trw.dbsubsetter.load

import trw.dbsubsetter.e2e.AbstractEndToEndTest
import trw.dbsubsetter.util.db.Database
import trw.dbsubsetter.util.runner.TestSubsetRunner

/*
 * A trw.dbsubsetter.load test is an AbstractEndToEndTest with two extra capabilities:
 *   1. Track how long a subsetting run took and assert that it was within expected limits
 *   2. Quickly trw.dbsubsetter.load data into the origin DB on the first run and then keep the container there for faster runs later
 *
 * This trait just handles (1) right now, and (2) is handled in concrete test classes in a very quickly-hacked-together
 * way. Because of how messy the implementation of (2) is at the moment, only Postgres trw.dbsubsetter.load tests work at the moment, and
 * their MySql and SqlServer counterparts are commented out. This is a definite area for future refactoring.
 */
trait LoadTest[T <: Database] { this: AbstractEndToEndTest[T] =>

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
