package e2e.validation.nonempty

import e2e.SqlServerEnabledTest

class NonEmptySchemaValidationTestSqlServer extends SqlServerEnabledTest with NonEmptySchemaValidationTest {

  override protected def additionalSchemas: Set[String] = Set("valid_schema")

}
