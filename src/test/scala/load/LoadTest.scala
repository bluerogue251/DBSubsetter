package load

import e2e.SubsettingTest
import util.db.Database

/**
 * A load test is a `SubsettingTest` with two extra capabilities:
 *   1. Track how long a subsetting run took and assert that it was within expected limits
 *   2. Quickly load data into the origin DB on the first run and then keep the container there for faster runs later
 *
 * This trait just handles (1) right now, and (2) is handled in concrete test classes in a very quickly-hacked-together
 * way. Because of how messy the implementation of (2) is at the moment, only Postgres load tests work at the moment, and
 * their MySql and SqlServer counterparts are commented out. This is a definite area for future refactoring.
 */
trait LoadTest[T <: Database] { this: SubsettingTest[T] =>

  protected def singleThreadedRuntimeLimitMillis: Long

  protected def akkaStreamsRuntimeLimitMillis: Long

  test("Check that single threaded runtime did not significantly increase") {
    assert(singleThreadedRuntimeMillis < singleThreadedRuntimeLimitMillis)
  }

  test("Check that Akka Streams runtime did not significantly increase") {
    assert(akkaStreamsRuntimeMillis < akkaStreamsRuntimeLimitMillis)
  }
}
