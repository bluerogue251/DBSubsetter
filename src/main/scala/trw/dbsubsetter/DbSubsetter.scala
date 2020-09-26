package trw.dbsubsetter

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{BaseQueries, DbMetadataQueries, SchemaInfoRetrieval}

import scala.concurrent.duration._

object DbSubsetter {
  def run(config: Config): Duration = {
    val startTime =
      System.nanoTime()

    val dbMetadata =
      DbMetadataQueries.retrieveSchemaMetadata(
        config.originDbConnectionString,
        config.schemas
      )

    val schemaInfo =
      SchemaInfoRetrieval.getSchemaInfo(dbMetadata, config)

    val baseQueries =
      BaseQueries.get(config, schemaInfo)

    if (config.singleThreadMode) {
      new ApplicationSingleThreaded(config, schemaInfo, baseQueries).run()
    } else {
      ApplicationAkkaStreams.run(config, schemaInfo, baseQueries)
    }

    (System.nanoTime() - startTime).nanoseconds
  }
}
