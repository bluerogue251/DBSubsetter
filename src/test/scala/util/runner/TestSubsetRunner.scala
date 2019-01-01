package util.runner

import trw.dbsubsetter.Application
import util.db.{Database, DatabaseContainerSet}

object TestSubsetRunner {

  def runSubsetInSingleThreadedMode[T <: Database](containers: DatabaseContainerSet[T], programArgs: Array[String]): Long = {
    val defaultArgs: Array[String] = Array(
      "--originDbConnStr", containers.origin.db.connectionString,
      "--targetDbConnStr", containers.targetSingleThreaded.db.connectionString,
      "--singleThreadedDebugMode"

    )
    val finalArgs: Array[String] = defaultArgs ++ programArgs

    val singleThreadedRuntimeMillis = timedSubsetMilliseconds(finalArgs)
    println(s"Single Threaded Took $singleThreadedRuntimeMillis milliseconds")

    singleThreadedRuntimeMillis
  }

  def runSubsetInAkkaStreamsMode[T <: Database](containers: DatabaseContainerSet[T], programArgs: Array[String]): Long = {
    val defaultArgs: Array[String] = Array(
      "--originDbConnStr", containers.origin.db.connectionString,
      "--originDbParallelism", "10",
      "--targetDbParallelism", "10",
      "--targetDbConnStr", containers.targetAkkaStreams.db.connectionString
    )
    val finalArgs: Array[String] = defaultArgs ++ programArgs

    val runtimeMillis: Long = timedSubsetMilliseconds(finalArgs)
    println(s"Akka Streams Took $runtimeMillis milliseconds")

    runtimeMillis
  }

  // TODO: refactor to re-use the timing logic already present in production code
  private def timedSubsetMilliseconds(args: Array[String]): Long = {
    val start = System.nanoTime()
    Application.main(args)
    val end = System.nanoTime()
    (end - start) / 1000000
  }
}
