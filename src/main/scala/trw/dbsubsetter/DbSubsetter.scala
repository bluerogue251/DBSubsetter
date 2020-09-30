package trw.dbsubsetter

import trw.dbsubsetter.config._
import trw.dbsubsetter.db.{BaseQueries, DbMetadataQueries, OK, SchemaInfoRetrieval, SchemaValidation, ValidationError}

object DbSubsetter {
  def run(cmdLineConfig: CommandLineConfig): DbSubsetterResult = {
    ConfigExtractor.extractSchemaConfig(cmdLineConfig) match {
      case InvalidInput(errorType) =>
        FailedSchemaConfigExtraction(errorType)

      case Valid(schemaConfig) =>
        val dbMetadata =
          DbMetadataQueries.retrieveSchemaMetadata(
            cmdLineConfig.originDbConnectionString,
            cmdLineConfig.schemas
          )

        SchemaValidation.validate(schemaConfig, dbMetadata) match {
          case ValidationError(message) =>
            FailedValidation(message)

          case OK =>
            val schemaInfo = SchemaInfoRetrieval.getSchemaInfo(dbMetadata, schemaConfig)
            val baseQueries = BaseQueries.get(schemaConfig, schemaInfo)
            cmdLineConfig.runMode match {
              case AkkaStreamsMode =>
                ApplicationAkkaStreams.run(cmdLineConfig, schemaInfo, baseQueries)
              case DebugMode =>
                new ApplicationSingleThreaded(cmdLineConfig, schemaInfo, baseQueries).run()
            }
            SubsetCompletedSuccessfully
        }
    }
  }

  sealed trait DbSubsetterResult
  case object SubsetCompletedSuccessfully extends DbSubsetterResult
  case class FailedSchemaConfigExtraction(error: SchemaConfigError) extends DbSubsetterResult
  case class FailedValidation(message: String) extends DbSubsetterResult
}
