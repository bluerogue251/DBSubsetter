package util.runner

import trw.dbsubsetter.config.{CommandLineParser, Config}
import trw.dbsubsetter.db.{SchemaInfo, SchemaInfoRetrieval}
import trw.dbsubsetter.workflow.BaseQueries
import trw.dbsubsetter.{ApplicationAkkaStreams, ApplicationSingleThreaded}
import util.db.{Database, DatabaseContainerSet}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object TestSubsetRunner {

  def runSubsetInSingleThreadedMode[T <: Database](containers: DatabaseContainerSet[T], programArgs: Array[String]): Long = {
    val defaultArgs: Array[String] = Array(
      "--originDbConnStr", containers.origin.db.connectionString,
      "--targetDbConnStr", containers.targetSingleThreaded.db.connectionString
    )
    val finalArgs: Array[String] = defaultArgs ++ programArgs

    val config: Config = CommandLineParser.parser.parse(finalArgs, Config()).get
    val schemaInfo: SchemaInfo = SchemaInfoRetrieval.getSchemaInfo(config)
    val baseQueries = BaseQueries.get(config, schemaInfo)

    val startSingleThreaded = System.nanoTime()
    ApplicationSingleThreaded.run(config, schemaInfo, baseQueries)
    val singleThreadedRuntimeMillis = (System.nanoTime() - startSingleThreaded) / 1000000
    println(s"Single Threaded Took $singleThreadedRuntimeMillis milliseconds")

    singleThreadedRuntimeMillis
  }

  def runSubsetInAkkaStreamsMode[T <: Database](containers: DatabaseContainerSet[T], programArgs: Array[String]): Long = {
    val defaultArgs: Array[String] = Array(
      "--originDbConnStr", containers.origin.db.connectionString,
      "--originDbParallelism", "2",
      "--targetDbParallelism", "2",
      "--targetDbConnStr", containers.targetAkkaStreams.db.connectionString
    )
    val finalArgs: Array[String] = defaultArgs ++ programArgs

    val config: Config = CommandLineParser.parser.parse(finalArgs, Config()).get
    val schemaInfo: SchemaInfo = SchemaInfoRetrieval.getSchemaInfo(config)
    val baseQueries = BaseQueries.get(config, schemaInfo)

    val startAkkaStreams = System.nanoTime()
    val futureResult = ApplicationAkkaStreams.run(config, schemaInfo, baseQueries)
    Await.result(futureResult, Duration.Inf)
    val akkStreamsRuntimeMillis = (System.nanoTime() - startAkkaStreams) / 1000000
    println(s"Akka Streams Took $akkStreamsRuntimeMillis milliseconds")

    akkStreamsRuntimeMillis
  }
}
