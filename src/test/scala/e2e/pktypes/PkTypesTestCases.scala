package e2e.pktypes

import java.util.UUID

import e2e.{AbstractEndToEndTest, SlickSetup}

trait PkTypesTestCases extends AbstractEndToEndTest with PkTypesDDL with SlickSetup {
  val dataSetName = "pk_types"

  import profile.api._

  override lazy val ddl = schema.create
  override lazy val dml = new PkTypesDML(profile).dbioSeq

  test("Correct byte_pk_table records were included") {
    assertResult(BytePkTable.map(_.id).sorted.result, Seq[Byte](-128, 0, 127))
  }

  test("Correct short_pk_table records were included") {
    assertResult(ShortPkTable.map(_.id).sorted.result, Seq[Short](-32768, 1, 32767))
  }

  test("Correct int_pk_table records were included") {
    assertCount(IntPkTable, 3)
    assertResult(IntPkTable.map(_.id).sorted.result, Seq[Int](-2147483648, 0, 2147483647))
  }

  test("Correct long_pk_table records were included") {
    assertResult(LongPkTable.map(_.id).sorted.result, Seq[Long](-9223372036854775808L, 1, 9223372036854775807L))
  }

  test("Correct uuid_pk_table records were included") {
    assertResult(UUIDPkTable.map(_.id).sorted.result, Seq[UUID](
      UUID.fromString("ae2c53e6-bef2-42cb-aaf7-3bdd58b0b645"),
      UUID.fromString("1607ce51-dcd1-480a-99a1-78027b654f50")
    ))
  }

  test("Correct char_10_pk_table records were included") {
    assertResult(Char10PkTable.map(_.id).sorted.result, Seq[String](" four ", "two "))
  }

  test("Correct varchar_10_pk_table records were included") {
    assertResult(Varchar10PkTable.map(_.id).sorted.result, Seq[String](" eight ", "six "))
  }
}