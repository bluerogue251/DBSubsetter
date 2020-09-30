package trw.dbsubsetter

import trw.dbsubsetter.config._
import trw.dbsubsetter.db.{BaseQueries, DbMetadataQueries, OK, SchemaInfoRetrieval, SchemaValidation, ValidationError}

object DbSubsetter {
  def run(input: CommandLineConfig): DbSubsetterResult = {
    ConfigExtractor.extractSchemaConfig(input) match {
      case InvalidInput(errorType) =>
        FailedSchemaConfigExtraction(errorType)

      case Valid(schemaConfig) =>
        val dbMetadata =
          DbMetadataQueries.retrieveSchemaMetadata(
            input.originDbConnectionString,
            input.schemas
          )

        SchemaValidation.validate(schemaConfig, dbMetadata) match {
          case ValidationError(message) =>
            FailedValidation(message)

          case OK =>
            val schemaInfo = SchemaInfoRetrieval.getSchemaInfo(dbMetadata, schemaConfig)
            val baseQueries = BaseQueries.get(schemaConfig, schemaInfo)
            val config = Config(
              originDbConnectionString = input.originDbConnectionString,
              targetDbConnectionString = input.targetDbConnectionString,
              keyCalculationDbConnectionCount = input.keyCalculationDbConnectionCount,
              dataCopyDbConnectionCount = input.dataCopyDbConnectionCount,
              tempfileStorageDirectoryOverride = input.tempfileStorageDirectoryOverride
            )
            input.runMode match {
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
  case class FailedSchemaConfigExtraction(error: SchemaConfigError) extends DbSubsetterResult
  case class FailedValidation(message: String) extends DbSubsetterResult
}
