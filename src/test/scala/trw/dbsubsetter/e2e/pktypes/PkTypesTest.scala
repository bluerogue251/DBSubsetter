package trw.dbsubsetter.e2e.pktypes

import java.util.UUID

import org.scalatest.FunSuiteLike
import trw.dbsubsetter.util.assertion.AssertionUtil
import trw.dbsubsetter.util.slick.SlickUtil

trait PkTypesTest extends FunSuiteLike with AssertionUtil {
  val testName = "pk_types"

  protected val profile: slick.jdbc.JdbcProfile

  protected def originSlick: slick.jdbc.JdbcBackend#DatabaseDef

  private val ddl: PkTypesDDL = new PkTypesDDL(profile)

  import ddl.profile.api._

  protected def prepareOriginDDL(): Unit = {
    SlickUtil.ddl(originSlick, ddl.schema.create)
  }

  protected def prepareOriginDML(): Unit = {
    SlickUtil.dml(originSlick, PkTypesDML.dbioSeq(ddl))
  }

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
    assertResult(ddl.BytePkTable.map(_.id).sorted.result, expectedByteIds)
  }

  test("Correct short_pk_table records were included") {
    assertResult(ddl.ShortPkTable.map(_.id).sorted.result, Seq[Short](-32768, 1, 32767))
  }

  test("Correct int_pk_table records were included") {
    assertResult(ddl.IntPkTable.map(_.id).sorted.result, Seq[Int](-2147483648, 1, 2147483647))
  }

  test("Correct long_pk_table records were included") {
    assertResult(ddl.LongPkTable.map(_.id).sorted.result, Seq[Long](-9223372036854775808L, 1, 9223372036854775807L))
  }

  test("Correct uuid_pk_table records were included") {
    assertResult(ddl.UUIDPkTable.map(_.id).sorted.result, expectedUUIDs)
  }

  test("Correct char_10_pk_table records were included") {
    assertResult(ddl.Char10PkTable.map(_.id).sorted.result, expectedChar10Ids)
  }

  test("Correct varchar_10_pk_table records were included") {
    assertResult(ddl.Varchar10PkTable.map(_.id).sorted.result, Seq[String](" eight ", "six "))
  }

  test("Correct referencing_table records were included") {
    assertResult(ddl.ReferencingTable.map(_.id).sorted.result, expectedReferencingTableIds)
  }
}