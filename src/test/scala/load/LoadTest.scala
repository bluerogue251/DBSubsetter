package load

import e2e.SubsettingTest
import util.db.Database

import scala.concurrent.duration.Duration

/**
  * A load test is a `SubsettingTest` with two extra capabilities:
  *   1. Assert that subsetting runtimes were within some pre-defined, expected limits
  *   2. Quickly load data into the origin DB on the first run and then keep the container there for faster runs later
  *
  * This trait just handles (1) right now, and (2) is handled in concrete test classes in a very quickly-hacked-together
  * way. Because of how messy the implementation of (2) is at the moment, only Postgres load tests work at the moment, and
  * their MySql and SqlServer counterparts are commented out. This is a definite area for future refactoring.
  */
trait LoadTest[T <: Database] extends SubsettingTest[T] {

  /**
    * The maximum amount of time the subsetting run is allowed to take in its normal running mode
    */
  protected def akkaStreamsModeLimit: Duration

  test("Check that Akka Streams runtime did not significantly increase") {
    assert(subsettingResult.runDuration < akkaStreamsModeLimit)
  }
}
