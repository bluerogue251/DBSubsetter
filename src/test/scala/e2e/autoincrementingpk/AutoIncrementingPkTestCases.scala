package e2e.autoincrementingpk

import e2e.{AbstractEndToEndTest, SlickSetup}

trait AutoIncrementingPkTestCases extends AbstractEndToEndTest with AutoIncrementingPkDDL with SlickSetup {
  val dataSetName = "autoincrementing_pk"

  import profile.api._

  override lazy val ddl = schema.create
  override lazy val dml = new AutoIncrementingPkDML(profile).dbioSeq

  test("Correct records were included and their primary keys values are correct") {
    assertCount(AutoincrementingPkTable, 10)
    // Tests that target databases don't over-write the primary key values
    // with the default values from their own primary key sequences.
    // The correct values for the primary keys are: 2, 4, 6, 8, 10, 12, 14, 16, 18, 20
    // If the target database overwrites PK values, we would see: 1, 2, 3, 4, 5, 6, etc.
    assertThat(AutoincrementingPkTable.map(_.id).sum.result, 110)
  }
}
