package e2e.crossschema

import e2e.MySqlSubsettingTest

class CrossSchemaTestMySql extends MySqlSubsettingTest with CrossSchemaTest {
  // format: off
  
  override protected def additionalSchemas: Set[String] =
    Set("schema_1", "schema_2", "schema_3")
  
  override protected val programArgs = Array(
    "--schemas", "schema_1, schema_2,schema_3",
    "--baseQuery", "schema_1.schema_1_table ::: id = 2 ::: includeChildren"
  )
}
