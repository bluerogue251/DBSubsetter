package trw.dbsubsetter.db

import trw.dbsubsetter.config.SchemaConfig

object SchemaValidation {

  def validate(schemaConfig: SchemaConfig, dbMeta: DbMetadataQueryResult): SchemaValidationResult = {
    val actualSchemas: Set[Schema] =
      dbMeta.schemas
        .map(schemaQueryRow => Schema(schemaQueryRow.name))
        .toSet

    /*
     * Detect Missing Schemas
     */
    val missingSchemas: Set[Schema] = schemaConfig.schemas.filterNot(actualSchemas)

    if (missingSchemas.nonEmpty) {
      val csv = missingSchemas.map(_.name).mkString(", ")
      return ValidationError(s"Specified --schemas not found in database: $csv")
    }

    val actualTables: Set[Table] =
      dbMeta.tables
        .map(tableQueryRow => Table(Schema(tableQueryRow.schema), tableQueryRow.name))
        .toSet

    /*
     * Detect missing tables from base queries
     */
    schemaConfig.baseQueries.foreach { baseQuery =>
      if (!actualTables.contains(baseQuery.table)) {
        return ValidationError(s"Table '${}' specified in --baseQuery not found in database")
      }
    }

    OK
  }
}

sealed trait SchemaValidationResult
case object OK extends SchemaValidationResult
case class ValidationError(message: String) extends SchemaValidationResult
