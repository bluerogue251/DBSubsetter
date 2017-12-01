package e2e.selfreferencing

import e2e.{AbstractEndToEndTest, SlickSetup}

trait SelfReferencingTestCases extends AbstractEndToEndTest with SelfReferencingDDL with SlickSetup {
  val dataSetName = "self_referencing"

  import profile.api._

  override lazy val ddl = schema.create
  override lazy val dml = new SelfReferencingDML(profile).dbioSeq

  test("Correct self_referencing_table records were included") {
    assertCount(SelfReferencingTable, 10)
    assertThat(SelfReferencingTable.map(_.id).sum.result, 70)
  }
}
