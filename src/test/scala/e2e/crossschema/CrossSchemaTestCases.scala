package e2e.crossschema

import e2e.SlickSetup
import org.scalatest.FunSuiteLike
import util.assertion.AssertionUtil

trait CrossSchemaTestCases extends FunSuiteLike with CrossSchemaDDL with SlickSetup with AssertionUtil {
  protected val testName: String = "cross_schema"

  import profile.api._

  override protected lazy val ddl = schema.create

  override protected lazy val dml = new CrossSchemaDML(profile).dbioSeq

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