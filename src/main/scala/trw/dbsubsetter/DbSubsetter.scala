package trw.dbsubsetter

import trw.dbsubsetter.config.{AkkaStreamsMode, Config, SingleThreadMode}
import trw.dbsubsetter.db.{BaseQueries, DbMetadataQueries, OK, SchemaInfoRetrieval, SchemaValidation, ValidationError}

object DbSubsetter {
  def run(config: Config): Result = {
    val dbMetadata =
      DbMetadataQueries.retrieveSchemaMetadata(
        config.originDbConnectionString,
        config.schemas.map(_.name).toSet
      )

    SchemaValidation.validate(config, dbMetadata) match {
      case ValidationError(message) =>
        FailedValidation(message)
      case OK =>
        val schemaInfo = SchemaInfoRetrieval.getSchemaInfo(dbMetadata, config)
        val baseQueries = BaseQueries.get(config, schemaInfo)
        config.runMode match {
          case AkkaStreamsMode =>
            ApplicationAkkaStreams.run(config, schemaInfo, baseQueries)
          case SingleThreadMode =>
            new ApplicationSingleThreaded(config, schemaInfo, baseQueries).run()
        }
        Success
    }
  }
}

sealed trait Result

case object Success extends Result

case class FailedValidation(message: String) extends Result
