package trw.dbsubsetter

import trw.dbsubsetter.config._
import trw.dbsubsetter.db.{BaseQueries, DbMetadataQueries, OK, SchemaInfoRetrieval, SchemaValidation, ValidationError}

object DbSubsetter {
  def run(args: CommandLineArgs): DbSubsetterResult = {
    ConfigExtractor.extractSchemaConfig(args) match {
      case InvalidInput(errorType) =>
        FailedSchemaConfigExtraction(errorType)

      case Valid(schemaConfig, config) =>
        val dbMetadata =
          DbMetadataQueries.retrieveSchemaMetadata(
            args.originDbConnectionString,
            args.schemas
          )

        SchemaValidation.validate(schemaConfig, dbMetadata) match {
          case ValidationError(message) =>
            FailedValidation(message)

          case OK =>
            val schemaInfo = SchemaInfoRetrieval.getSchemaInfo(dbMetadata, schemaConfig)
            val baseQueries = BaseQueries.get(schemaConfig, schemaInfo)
            args.runMode match {
              case AkkaStreamsMode =>
                ApplicationAkkaStreams.run(config, schemaInfo, baseQueries)
              case DebugMode =>
                new ApplicationSingleThreaded(config, schemaInfo, baseQueries).run()
            }
            SubsetCompletedSuccessfully
        }
    }
  }

  sealed trait DbSubsetterResult
  case object SubsetCompletedSuccessfully extends DbSubsetterResult
  case class FailedSchemaConfigExtraction(error: InvalidInputType) extends DbSubsetterResult
  case class FailedValidation(message: String) extends DbSubsetterResult
}
