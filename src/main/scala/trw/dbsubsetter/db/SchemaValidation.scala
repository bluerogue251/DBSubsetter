package trw.dbsubsetter.db

import trw.dbsubsetter.config.Config

object SchemaValidation {

  def validate(config: Config, dbMeta: DbMetadataQueryResult): SchemaValidationResult = {
    firstNotFoundSchemaName(config, dbMeta)
      .map(schemaName => ValidationError(s"specified schema not found: $schemaName"))
      .getOrElse(OK)
  }

  private[this] def firstNotFoundSchemaName(config: Config, dbMeta: DbMetadataQueryResult): Option[String] = {
    val actualSchemas: Set[String] = dbMeta.schemas.map(_.name).toSet
    val configSchemas: Set[String] = config.schemas.map(_.name).toSet
    configSchemas.filterNot(actualSchemas).headOption
  }
}

sealed trait SchemaValidationResult
case object OK extends SchemaValidationResult
case class ValidationError(message: String) extends SchemaValidationResult
