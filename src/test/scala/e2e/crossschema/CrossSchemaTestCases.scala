package e2e.crossschema

import e2e.{AbstractEndToEndTest, SlickSetup}

trait CrossSchemaTestCases extends AbstractEndToEndTest with CrossSchemaDDL with SlickSetup {
  val dataSetName = "cross_schema"

  import profile.api._

  override lazy val ddl = schema.create
  override lazy val dml = new CrossSchemaDML(profile).dbioSeq

  test("Correct table 1 records were included") {
    assertCount(Schema1Table, 1)
    assertThat(Schema1Table.map(_.id).sum.result, 2)
  }

  test("Correct table 2 records were included") {
    assertCount(Schema2Table, 1)
    assertThat(Schema2Table.map(_.id).sum.result, 2)
  }

  test("Correct table 3 records were included") {
    assertCount(Schema3Table, 2)
    assertThat(Schema3Table.map(_.id).sum.result, 7)
  }
}