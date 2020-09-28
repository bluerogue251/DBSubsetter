package trw.dbsubsetter.db

import trw.dbsubsetter.config.Config

object SchemaValidation {

  def validate(config: Config, dbMeta: DbMetadataQueryResult): SchemaValidationResult = {
    /*
     * Detect Missing Schemas
     */
    val missingSchemaNames: Set[String] = findMissingSchemas(config, dbMeta)

    if (missingSchemaNames.size == 1) {
      return ValidationError(s"Specified schema not found: ${missingSchemaNames.head}")
    }

    if (missingSchemaNames.size > 1) {
      val csv = missingSchemaNames.mkString(", ")
      return ValidationError(s"Specified schemas not found: $csv")
    }

    OK
  }

  private[this] def findMissingSchemas(config: Config, dbMeta: DbMetadataQueryResult): Set[String] = {
    val actualSchemas: Set[String] = dbMeta.schemas.map(_.name).toSet
    val configSchemas: Set[String] = config.schemas.map(_.name).toSet
    configSchemas.filterNot(actualSchemas)
  }
}

sealed trait SchemaValidationResult
case object OK extends SchemaValidationResult
case class ValidationError(message: String) extends SchemaValidationResult
