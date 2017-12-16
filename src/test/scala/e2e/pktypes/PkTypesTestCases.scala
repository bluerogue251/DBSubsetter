package e2e.pktypes

import e2e.{AbstractEndToEndTest, SlickSetup}

trait PkTypesTestCases extends AbstractEndToEndTest with PkTypesDDL with SlickSetup {
  val dataSetName = "pk_types"

  import profile.api._

  override lazy val ddl = schema.create
  override lazy val dml = new PkTypesDML(profile).dbioSeq

  test("Correct table 1 records were included") {
    ???
  }

  test("Correct table 2 records were included") {
    ???
  }
}