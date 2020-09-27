package trw.dbsubsetter

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{BaseQueries, DbMetadataQueries, SchemaInfoRetrieval}

object DbSubsetter {
  def run(config: Config): Result = {
    val dbMetadata =
      DbMetadataQueries.retrieveSchemaMetadata(
        config.originDbConnectionString,
        config.schemas.map(_.name).toSet
      )

    val schemaInfo =
      SchemaInfoRetrieval.getSchemaInfo(dbMetadata, config)

    val baseQueries =
      BaseQueries.get(config, schemaInfo)

    if (config.singleThreadMode) {
      new ApplicationSingleThreaded(config, schemaInfo, baseQueries).run()
      Success
    } else {
      ApplicationAkkaStreams.run(config, schemaInfo, baseQueries)
      Success
    }
  }
}

sealed trait Result

case object Success extends Result

case class FailedValidation(message: String) extends Result
