package e2e

import trw.dbsubsetter.{ApplicationRunResult, ApplicationRunner}
import util.db.Database

import scala.concurrent.duration.{Duration, DurationLong}

/**
  * A test mixin which runs subsetting before starting to make assertions.
  * Tests may then make assertions about the resulting target dataset.
  */
trait SubsettingTest[T <: Database] extends DbEnabledTest[T] {

  protected def programArgs: Array[String]

  protected var subsettingResult: TestRunResult = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    subsettingResult = runSubsetting()
  }

  protected def runSubsetting(): TestRunResult = {
    // format: off
    val defaultArgs: Array[String] = Array(
      "--originDbConnStr", dbs.origin.connectionString,
      "--keyCalculationDbConnectionCount", "10",
      "--dataCopyDbConnectionCount", "10",
      "--targetDbConnStr", dbs.target.connectionString,
      "--exposeMetrics"
    )
    // format: on
    val finalArgs: Array[String] = defaultArgs ++ programArgs

    val result = timedSubsetMilliseconds(finalArgs)
    println(s"Akka Streams Mode Result: $result")
    result
  }

  private def timedSubsetMilliseconds(args: Array[String]): TestRunResult = {
    val start = System.nanoTime()
    val applicationRunResult = ApplicationRunner.run(args)
    val duration = (System.nanoTime() - start).nanos
    TestRunResult(applicationRunResult, duration)
  }

  case class TestRunResult(runResult: ApplicationRunResult, runDuration: Duration)
}
