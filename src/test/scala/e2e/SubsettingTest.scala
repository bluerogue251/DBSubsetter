package e2e

import trw.dbsubsetter.{ApplicationRunResult, ApplicationRunner}
import util.db.Database

import scala.concurrent.duration.{Duration, DurationLong}

/**
  * A test mixin which runs subsetting in both single threaded mode and akka streams
  * mode before starting to make assertions. Tests may then make assertions about
  * the resulting target datasets.
  */
trait SubsettingTest[T <: Database] extends DbEnabledTest[T] {

  protected def programArgs: Array[String]

  protected var debugModeResult: TestRunResult = _

  protected var akkaStreamsModeResult: TestRunResult = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    debugModeResult = runSubsetInSingleThreadedMode()
    akkaStreamsModeResult = runSubsetInAkkaStreamsMode()
  }

  // format: off
  protected def runSubsetInSingleThreadedMode(): TestRunResult = {
    val defaultArgs: Array[String] = Array(
      "--originDbConnStr", dbs.origin.connectionString,
      "--targetDbConnStr", dbs.targetSingleThreaded.connectionString,
      "--singleThreadedDebugMode"
    )
    val finalArgs: Array[String] = defaultArgs ++ programArgs

    timedSubsetMilliseconds(finalArgs)
  }
  // format: on

  // format: off
  protected def runSubsetInAkkaStreamsMode(): TestRunResult = {
    val defaultArgs: Array[String] = Array(
      "--originDbConnStr", dbs.origin.connectionString,
      "--keyCalculationDbConnectionCount", "10",
      "--dataCopyDbConnectionCount", "10",
      "--targetDbConnStr", dbs.targetAkkaStreams.connectionString,
      "--exposeMetrics"
    )
    val finalArgs: Array[String] = defaultArgs ++ programArgs

    timedSubsetMilliseconds(finalArgs)
  }
  // format: on

  private def timedSubsetMilliseconds(args: Array[String]): TestRunResult = {
    val start = System.nanoTime()
    val result = ApplicationRunner.run(args)
    val duration = (System.nanoTime() - start).nanos
    TestRunResult(result, duration)
  }

  case class TestRunResult(runResult: ApplicationRunResult, runDuration: Duration)
}
