package e2e.validation

import org.scalatest.FunSuiteLike
import util.assertion.AssertionUtil
import util.slick.SlickUtil

trait ValidationTest extends FunSuiteLike with AssertionUtil {
  val testName = "self_referencing"

  protected val profile: slick.jdbc.JdbcProfile

  protected def originSlick: slick.jdbc.JdbcBackend#DatabaseDef

  private val ddl: ValidationDDL = new ValidationDDL(profile)

  import ddl.profile.api._

  protected def prepareOriginDDL(): Unit = {
    SlickUtil.ddl(originSlick, ddl.schema.create)
  }

  protected def prepareOriginDML(): Unit = {
    // No-Op
  }

  test("Correct self_referencing_table records were included") {
    assertCount(ddl.ValidationTableQuery, 10)
    assertThat(ddl.ValidationTableQuery.map(_.id).sum.result, 70)
  }
}
