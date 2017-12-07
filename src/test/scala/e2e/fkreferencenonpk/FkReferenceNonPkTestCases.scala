package e2e.fkreferencenonpk

import e2e.{AbstractEndToEndTest, SlickSetup}

trait FkReferenceNonPkTestCases extends AbstractEndToEndTest with FkReferenceNonPkDDL with SlickSetup {

  import profile.api._

  override val ddl = schema.create
  override val dml = new FkReferenceNonPkDML(profile).dbioSeq

  val dataSetName = "fk_reference_non_pk"

  test("Correct referenced_table records were included") {
    assertCount(ReferencedTable, 3)
    assertThat(ReferencedTable.map(_.id).sum.result, 10)
  }

  test("Correct referencing_table records were included") {
    assertCount(ReferencingTable, 7)
    assertThat(ReferencingTable.map(_.id).sum.result, 36)
  }
}
