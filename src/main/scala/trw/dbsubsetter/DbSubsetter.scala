package trw.dbsubsetter

import io.prometheus.client.exporter.HTTPServer
import io.prometheus.client.hotspot.DefaultExports
import trw.dbsubsetter.config._
import trw.dbsubsetter.db.{DbMetadataQueries, OK, SchemaInfoRetrieval, SchemaValidation, ValidationError}

object DbSubsetter {
  def run(schemaConfig: SchemaConfig, config: Config): DbSubsetterResult = {
    val dbMetadata =
      DbMetadataQueries.retrieveSchemaMetadata(
        config.originDbConnectionString,
        schemaConfig.schemas.map(_.name)
      )

    SchemaValidation.validate(schemaConfig, dbMetadata) match {
      case ValidationError(message) =>
        FailedValidation(message)

      case OK =>
        val schemaInfo = SchemaInfoRetrieval.getSchemaInfo(dbMetadata, schemaConfig)

        val metricsEndpoint: Option[HTTPServer] =
          config.metricsPort
            .map { port =>
              DefaultExports.initialize()
              new HTTPServer(port)
            }

        ApplicationAkkaStreams.run(config, schemaInfo, schemaConfig.baseQueries)
        metricsEndpoint.foreach(_.stop())
        SubsetCompletedSuccessfully
    }
  }

  sealed trait DbSubsetterResult
  case object SubsetCompletedSuccessfully extends DbSubsetterResult
  case class FailedValidation(message: String) extends DbSubsetterResult
}
