package trw.dbsubsetter.util.runner

import trw.dbsubsetter.Application
import trw.dbsubsetter.util.db.{Database, DatabaseContainerSet}

object TestSubsetRunner {

  def runSubsetInSingleThreadedMode[T <: Database](containers: DatabaseContainerSet[T], programArgs: Array[String]): Long = {
    val defaultArgs: Array[String] = Array(
      "--originDbConnStr", containers.origin.db.connectionString,
      "--targetDbConnStr", containers.targetSingleThreaded.db.connectionString,
      "--singleThreadedDebugMode",
      "--exposeMetrics"

    )
    val finalArgs: Array[String] = defaultArgs ++ programArgs

    timedSubsetMilliseconds(finalArgs)
  }

  def runSubsetInAkkaStreamsMode[T <: Database](containers: DatabaseContainerSet[T], programArgs: Array[String]): Long = {
    val defaultArgs: Array[String] = Array(
      "--originDbConnStr", containers.origin.db.connectionString,
      "--originDbParallelism", "10",
      "--targetDbParallelism", "10",
      "--targetDbConnStr", containers.targetAkkaStreams.db.connectionString,
      "--exposeMetrics"
    )
    val finalArgs: Array[String] = defaultArgs ++ programArgs

    timedSubsetMilliseconds(finalArgs)
  }

  // TODO: refactor to re-use the timing logic already present in production code
  private def timedSubsetMilliseconds(args: Array[String]): Long = {
    val start = System.nanoTime()
    Application.main(args)
    val end = System.nanoTime()
    (end - start) / 1000000
  }
}
