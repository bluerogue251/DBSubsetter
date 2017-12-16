package e2e.pktypes

import e2e.{AbstractEndToEndTest, SlickSetup}

trait PkTypesTestCases extends AbstractEndToEndTest with PkTypesDDL with SlickSetup {
  val dataSetName = "pk_types"

  import profile.api._

  override lazy val ddl = schema.create
  override lazy val dml = new PkTypesDML(profile).dbioSeq

  test("Correct byte_pk_table records were included") {
    assertCount(BytePkTable, 3)
    assertThatByte(BytePkTable.map(_.id).sum.result, -1)
  }

  test("Correct short_pk_table records were included") {
    assertCount(ShortPkTable, 3)
    assertThatShort(ShortPkTable.map(_.id).sum.result, -1)
  }

  test("Correct int_pk_table records were included") {
    assertCount(IntPkTable, 3)
  }

  test("Correct long_pk_table records were included") {
    assertCount(LongPkTable, 3)
  }

  test("Correct uuid_pk_table records were included") {
    assertCount(UUIDPkTable, 3)
  }

  test("Correct char_10_pk_table records were included") {
    assertCount(Char10PkTable, 3)
  }

  test("Correct varchar_10_pk_table records were included") {
    assertCount(Varchar10PkTable, 3)
  }
}