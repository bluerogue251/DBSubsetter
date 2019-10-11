package util.runner

import trw.dbsubsetter.ApplicationRunner
import util.db.{Database, DatabaseSet}

object TestSubsetRunner {

  def runSubsetInSingleThreadedMode[T <: Database](dbs: DatabaseSet[T], programArgs: Array[String]): Long = {
    val defaultArgs: Array[String] = Array(
      "--originDbConnStr", dbs.origin.connectionString,
      "--targetDbConnStr", dbs.targetSingleThreaded.connectionString,
      "--singleThreadedDebugMode"

    )
    val finalArgs: Array[String] = defaultArgs ++ programArgs

    timedSubsetMilliseconds(finalArgs)
  }

  def runSubsetInAkkaStreamsMode[T <: Database](containers: DatabaseSet[T], programArgs: Array[String]): Long = {
    val defaultArgs: Array[String] = Array(
      "--originDbConnStr", containers.origin.connectionString,
      "--originDbParallelism", "10",
      "--targetDbParallelism", "10",
      "--targetDbConnStr", containers.targetAkkaStreams.connectionString,
      "--exposeMetrics"
    )
    val finalArgs: Array[String] = defaultArgs ++ programArgs

    timedSubsetMilliseconds(finalArgs)
  }

  // TODO: refactor to re-use the timing logic already present in production code
  private def timedSubsetMilliseconds(args: Array[String]): Long = {
    val start = System.nanoTime()
    ApplicationRunner.run(args)
    val end = System.nanoTime()
    (end - start) / 1000000
  }
}
