package e2e.fkreferencenonpk

import e2e.SlickSetup
import org.scalatest.FunSuiteLike
import util.assertion.AssertionUtil

trait FkReferenceNonPkTestCases extends FunSuiteLike with FkReferenceNonPkDDL with SlickSetup with AssertionUtil {

  import profile.api._

  override val ddl = schema.create
  override val dml = new FkReferenceNonPkDML(profile).dbioSeq

  val testName = "fk_reference_non_pk"

  test("Correct referenced_table records were included") {
    assertCount(ReferencedTable, 3)
    assertThat(ReferencedTable.map(_.id).sum.result, 10)
  }

  test("Correct referencing_table records were included") {
    assertCount(ReferencingTable, 7)
    assertThat(ReferencingTable.map(_.id).sum.result, 36)
  }
}
