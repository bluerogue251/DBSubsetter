package trw.dbsubsetter.e2e.fktononpk

import org.scalatest.FunSuiteLike
import trw.dbsubsetter.util.assertion.AssertionUtil
import trw.dbsubsetter.util.slick.SlickUtil

trait ForeignKeyToNonPrimaryKeyTest extends FunSuiteLike with AssertionUtil {
  val testName = "fk_reference_non_pk"

  protected val profile: slick.jdbc.JdbcProfile

  protected def originSlick: slick.jdbc.JdbcBackend#DatabaseDef

  private val ddl: ForeignKeyToNonPrimaryKeyDDL = new ForeignKeyToNonPrimaryKeyDDL(profile)

  import ddl.profile.api._

  protected def prepareOriginDDL(): Unit = {
    SlickUtil.ddl(originSlick, ddl.schema.create)
  }

  protected def prepareOriginDML(): Unit = {
    SlickUtil.dml(originSlick, ForeignKeyToNonPrimaryKeyDML.dbioSeq(ddl))
  }

  test("Correct referenced_table records were included") {
    assertCount(ddl.ReferencedTable, 3)
    assertThat(ddl.ReferencedTable.map(_.id).sum.result, 10)
  }

  test("Correct referencing_table records were included") {
    assertCount(ddl.ReferencingTable, 7)
    assertThat(ddl.ReferencingTable.map(_.id).sum.result, 36)
  }
}
