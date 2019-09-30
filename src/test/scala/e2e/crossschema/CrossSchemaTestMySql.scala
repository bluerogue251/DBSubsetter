package e2e.crossschema

import e2e.AbstractMysqlEndToEndTest

class CrossSchemaTestMySql extends AbstractMysqlEndToEndTest with CrossSchemaTest {

  override protected val additionalSchemas: List[String] =
    List[String]("schema_1", "schema_2", "schema_3")

  override protected val programArgs = Array(
    "--schemas", "schema_1, schema_2,schema_3",
    "--baseQuery", "schema_1.schema_1_table ::: id = 2 ::: includeChildren"
  )
}
