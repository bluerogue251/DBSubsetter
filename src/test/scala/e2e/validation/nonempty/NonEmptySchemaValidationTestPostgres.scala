package e2e.validation.nonempty

import e2e.PostgresEnabledTest

class NonEmptySchemaValidationTestPostgres extends PostgresEnabledTest with NonEmptySchemaValidationTest {

  override protected def additionalSchemas: Set[String] = Set("valid_schema")

}
