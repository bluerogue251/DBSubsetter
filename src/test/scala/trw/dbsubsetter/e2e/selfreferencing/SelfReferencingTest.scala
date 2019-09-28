package trw.dbsubsetter.e2e.selfreferencing

import org.scalatest.FunSuiteLike
import trw.dbsubsetter.util.assertion.AssertionUtil
import trw.dbsubsetter.util.slick.SlickUtil

trait SelfReferencingTest extends FunSuiteLike with AssertionUtil {
  val testName = "self_referencing"

  protected val profile: slick.jdbc.JdbcProfile

  protected def originSlick: slick.jdbc.JdbcBackend#DatabaseDef

  private val ddl: SelfReferencingDDL = new SelfReferencingDDL(profile)

  import ddl.profile.api._

  protected def prepareOriginDDL(): Unit = {
    SlickUtil.ddl(originSlick, ddl.schema.create)
  }

  protected def prepareOriginDML(): Unit = {
    SlickUtil.dml(originSlick, SelfReferencingDML.dbioSeq(ddl))
  }

  test("Correct self_referencing_table records were included") {
    assertCount(ddl.SelfReferencingTable, 10)
    assertThat(ddl.SelfReferencingTable.map(_.id).sum.result, 70)
  }
}
