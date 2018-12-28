package e2e.pktypes

import java.util.UUID

import e2e.SlickSetup
import org.scalatest.FunSuiteLike
import util.assertion.AssertionUtil

trait PkTypesTestCases extends FunSuiteLike with PkTypesDDL with SlickSetup with AssertionUtil {
  val dataSetName = "pk_types"

  import profile.api._

  override lazy val ddl = schema.create
  override lazy val dml = new PkTypesDML(profile).dbioSeq

  def expectedUUIDs = Seq[UUID](
    UUID.fromString("1607ce51-dcd1-480a-99a1-78027b654f50"),
    UUID.fromString("ae2c53e6-bef2-42cb-aaf7-3bdd58b0b645")
  )

  def expectedByteIds = Seq[Byte](-128, 1, 127)

  def expectedChar10Ids = Seq[String](" four     ", "two       ")

  def expectedReferencingTableIds = Seq[Int](1, 3, 4, 5, 7, 8, 9, 11, 12, 13, 15, 16, 17, 18, 20, 21, 23, 24)

  // We purposely exclude rows with a PK of 0 to test for a subtle bug in which
  // we used to accidentally be casting `null` values to 0s due to using Scala's
  // Byte, Short, Int, Long instead of the nullable Java equivalents. When null
  // fk values showed up, they caused the `0` row to accidentally be included

  test("Correct byte_pk_table records were included") {
    assertResult(BytePkTable.map(_.id).sorted.result, expectedByteIds)
  }

  test("Correct short_pk_table records were included") {
    assertResult(ShortPkTable.map(_.id).sorted.result, Seq[Short](-32768, 1, 32767))
  }

  test("Correct int_pk_table records were included") {
    assertResult(IntPkTable.map(_.id).sorted.result, Seq[Int](-2147483648, 1, 2147483647))
  }

  test("Correct long_pk_table records were included") {
    assertResult(LongPkTable.map(_.id).sorted.result, Seq[Long](-9223372036854775808L, 1, 9223372036854775807L))
  }

  test("Correct uuid_pk_table records were included") {
    assertResult(UUIDPkTable.map(_.id).sorted.result, expectedUUIDs)
  }

  test("Correct char_10_pk_table records were included") {
    assertResult(Char10PkTable.map(_.id).sorted.result, expectedChar10Ids)
  }

  test("Correct varchar_10_pk_table records were included") {
    assertResult(Varchar10PkTable.map(_.id).sorted.result, Seq[String](" eight ", "six "))
  }

  test("Correct referencing_table records were included") {
    assertResult(ReferencingTable.map(_.id).sorted.result, expectedReferencingTableIds)
  }
}