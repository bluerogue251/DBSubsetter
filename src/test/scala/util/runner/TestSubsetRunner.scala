package util.runner

import trw.dbsubsetter.ApplicationRunner
import util.db.{Database, DatabaseContainerSet}

object TestSubsetRunner {

  def runSubsetInSingleThreadedMode[T <: Database](containers: DatabaseContainerSet[T], programArgs: Array[String]): Long = {
    val defaultArgs: Array[String] = Array(
      "--originDbConnStr", containers.origin.db.connectionString,
      "--targetDbConnStr", containers.targetSingleThreaded.db.connectionString,
      "--singleThreadedDebugMode"

    )
    val finalArgs: Array[String] = defaultArgs ++ programArgs

    timedSubsetMilliseconds(finalArgs)
  }

  def runSubsetInAkkaStreamsMode[T <: Database](containers: DatabaseContainerSet[T], programArgs: Array[String]): Long = {
    val defaultArgs: Array[String] = Array(
      "--originDbConnStr", containers.origin.db.connectionString,
      "--originDbParallelism", "10",
      "--targetDbParallelism", "10",
      "--targetDbConnStr", containers.targetAkkaStreams.db.connectionString
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
