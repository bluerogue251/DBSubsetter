package trw.dbsubsetter.config

import trw.dbsubsetter.db.Schema

/**
  * Extracts command line supplied config values which are simple types like strings into
  * richer types. If invalid input prevents this, surface this as a validation error.
  */
class ConfigExtractor {
  def extractSchemaConfig(commandLineConfig: CommandLineConfig): ValidatedSchemaConfig = {

    val schemas = commandLineConfig.schemas.map(Schema)

    Valid(
      SchemaConfig(
        schemas = schemas,
        baseQueries =
      )
    )
  }
}

sealed trait ValidatedSchemaConfig
case class Valid(schemaConfig: SchemaConfig)
case class Invalid()
