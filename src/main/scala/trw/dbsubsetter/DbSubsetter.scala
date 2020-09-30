package trw.dbsubsetter

import trw.dbsubsetter.config._
import trw.dbsubsetter.db.{BaseQueries, DbMetadataQueries, OK, SchemaInfoRetrieval, SchemaValidation, ValidationError}

object DbSubsetter {
  def run(input: CommandLineConfig): DbSubsetterResult = {

    val schemaConfig =
      ConfigExtractor.extractSchemaConfig(input) match {
        case Valid(schemaConfig) =>
          schemaConfig
        case InvalidInput(errorType) =>
          return FailedSchemaConfigExtraction
      }

    val dbMetadata =
      DbMetadataQueries.retrieveSchemaMetadata(
        input.originDbConnectionString,
        input.schemas
      )

    SchemaValidation.validate(input, dbMetadata) match {
      case ValidationError(message) =>
        FailedValidation(message)
      case OK =>
        val schemaInfo = SchemaInfoRetrieval.getSchemaInfo(dbMetadata, input)
        val baseQueries = BaseQueries.get(input, schemaInfo)
        input.runMode match {
          case AkkaStreamsMode =>
            ApplicationAkkaStreams.run(input, schemaInfo, baseQueries)
          case DebugMode =>
            new ApplicationSingleThreaded(input, schemaInfo, baseQueries).run()
        }
        SubsetCompletedSuccessfully
    }
  }

  sealed trait DbSubsetterResult
  case object SubsetCompletedSuccessfully extends DbSubsetterResult
  case class FailedSchemaConfigExtraction(error: SchemaConfigError)
  case class FailedValidation(message: String) extends DbSubsetterResult
}
