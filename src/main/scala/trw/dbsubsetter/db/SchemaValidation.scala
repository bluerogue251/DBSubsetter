package trw.dbsubsetter.db

import trw.dbsubsetter.config.SchemaConfig

object SchemaValidation {

  def validate(schemaConfig: SchemaConfig, dbMeta: DbMetadataQueryResult): SchemaValidationResult = {
    /*
     * Detect Missing Schemas
     */
    val missingSchemaNames: Set[String] = findMissingSchemas(schemaConfig, dbMeta)

    if (missingSchemaNames.size == 1) {
      return ValidationError(s"Specified schema not found: ${missingSchemaNames.head}")
    }

    if (missingSchemaNames.size > 1) {
      val csv = missingSchemaNames.mkString(", ")
      return ValidationError(s"Specified schemas not found: $csv")
    }

    OK
  }

  private[this] def findMissingSchemas(schemaConfig: SchemaConfig, dbMeta: DbMetadataQueryResult): Set[String] = {
    val actualSchemas: Set[String] = dbMeta.schemas.map(_.name).toSet
    val configSchemas: Set[String] = schemaConfig.schemas.map(_.name)
    configSchemas.filterNot(actualSchemas)
  }
}

sealed trait SchemaValidationResult
case object OK extends SchemaValidationResult
case class ValidationError(message: String) extends SchemaValidationResult
