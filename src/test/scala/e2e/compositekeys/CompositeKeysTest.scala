package e2e.compositekeys

import org.scalatest.FunSuiteLike
import util.assertion.AssertionUtil
import util.slick.SlickUtil

trait CompositeKeysTest extends FunSuiteLike with AssertionUtil {
  val testName = "composite_keys"

  protected val profile: slick.jdbc.JdbcProfile

  protected def originSlick: slick.jdbc.JdbcBackend#DatabaseDef

  private val ddl: CompositeKeysDDL = new CompositeKeysDDL(profile)

  import ddl.profile.api._

  protected def prepareOriginDDL(): Unit = {
    SlickUtil.ddl(originSlick, ddl.schema.create)
  }

  protected def prepareOriginDML(): Unit = {
    SlickUtil.dml(originSlick, CompositeKeysDML.dbioSeq(ddl))
  }

  test("Correct parent table records were included") {
    assertCount(ddl.ParentTable, 2)
    assertResult(ddl.ParentTable.map(_.idOne).sorted.result, Seq[Int](1, 5))
    assertResult(ddl.ParentTable.map(_.idTwo).sorted.result, Seq[Int](2, 6))
  }

  test("Correct child table records were included") {
    assertCount(ddl.ChildTable, 5)
    assertResult(ddl.ChildTable.map(_.id).sorted.result, Seq[Int](100, 101, 106, 107, 108))
  }
}