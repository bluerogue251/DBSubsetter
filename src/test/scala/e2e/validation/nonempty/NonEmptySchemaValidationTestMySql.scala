package e2e.validation.nonempty

import e2e.MySqlEnabledTest

class NonEmptySchemaValidationTestMySql extends MySqlEnabledTest with NonEmptySchemaValidationTest {

  override protected def additionalSchemas: Set[String] = Set("valid_schema")

}
