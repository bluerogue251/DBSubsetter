package e2e

import trw.dbsubsetter.ApplicationRunner
import util.db.Database


trait SubsettingTest[T <: Database] extends DbEnabledTest[T] {

  protected def programArgs: Array[String]

  protected var singleThreadedRuntimeMillis: Long = _

  protected var akkaStreamsRuntimeMillis: Long = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    singleThreadedRuntimeMillis = runSubsetInSingleThreadedMode()
    akkaStreamsRuntimeMillis = runSubsetInAkkaStreamsMode()
  }

  protected def runSubsetInSingleThreadedMode(): Long = {
    val defaultArgs: Array[String] = Array(
      "--originDbConnStr", dbs.origin.connectionString,
      "--targetDbConnStr", dbs.targetSingleThreaded.connectionString,
      "--singleThreadedDebugMode"

    )
    val finalArgs: Array[String] = defaultArgs ++ programArgs

    timedSubsetMilliseconds(finalArgs)
  }

  protected def runSubsetInAkkaStreamsMode(): Long = {
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

  private def timedSubsetMilliseconds(args: Array[String]): Long = {
    val start = System.nanoTime()
    ApplicationRunner.run(args)
    val end = System.nanoTime()
    (end - start) / 1000000
  }
}
