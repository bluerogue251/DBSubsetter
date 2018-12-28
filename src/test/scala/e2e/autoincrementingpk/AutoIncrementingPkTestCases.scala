package e2e.autoincrementingpk

import e2e.SlickSetup
import org.scalatest.FunSuiteLike
import util.assertion.AssertionUtil

trait AutoIncrementingPkTestCases extends FunSuiteLike with AutoIncrementingPkDDL with SlickSetup with AssertionUtil {
  val testName = "autoincrementing_pk"

  import profile.api._

  override lazy val ddl = schema.create
  override lazy val dml = new AutoIncrementingPkDML(profile).dbioSeq

  test("Correct records were included for main table and their primary keys values are correct") {
    assertCount(AutoincrementingPkTable, 10)
    // Tests that target databases don't over-write the primary key values
    // with the default values from their own primary key sequences.
    // The correct values for the primary keys are: 2, 4, 6, 8, 10, 12, 14, 16, 18, 20
    // If the target database overwrites PK values, we would see: 1, 2, 3, 4, 5, 6, etc.
    assertThat(AutoincrementingPkTable.map(_.id).sum.result, 110)
  }

  // Purposely including a second table because it is a known limitation of MS SQl Server
  // that only one table at a time can have auto increment disabled for inserts
  // I'm actually surprised it works, seeing as we never unset the IDENTITY_INSERT setting
  // for any tables after I've set them, so I would expect things to error out on any table except the first
  // But for some reason it does appear to work.
  test("Correct records were included for other table and their primary keys values are correct") {
    assertCount(OtherAutoincrementingPkTable, 10)
    assertThat(OtherAutoincrementingPkTable.map(_.id).sum.result, 110)
  }
}
