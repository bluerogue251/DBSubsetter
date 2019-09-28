package trw.dbsubsetter.e2e.circulardep

import org.scalatest.FunSuiteLike
import trw.dbsubsetter.util.assertion.AssertionUtil
import trw.dbsubsetter.util.slick.SlickUtil

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait CircularDepTest extends FunSuiteLike with AssertionUtil {
  val testName = "circular_dep"

  protected val profile: slick.jdbc.JdbcProfile

  protected def originSlick: slick.jdbc.JdbcBackend#DatabaseDef

  private val ddl: CircularDepDDL = new CircularDepDDL(profile)

  import ddl.profile.api._

  protected def prepareOriginDDL(): Unit = {
    SlickUtil.ddl(originSlick, ddl.schema.create)
  }

  protected def prepareOriginDML(): Unit = {
    SlickUtil.dml(originSlick, CircularDepDML.dbioSeq(ddl))
  }

  test("Correct number of grandparents were included") {
    assertCount(ddl.Grandparents, 2)
  }

  test("All grandparents have correct number of parents") {
    (0 to 10 by 6).foreach { i =>
      assert(Await.result(targetSingleThreadedSlick.run(ddl.Parents.filter(_.grandparentId === i).size.result), Duration.Inf) === 10)
      assert(Await.result(targetAkkaStreamsSlick.run(ddl.Parents.filter(_.grandparentId === i).size.result), Duration.Inf) === 10)
    }
  }

  test("All parents have correct number of children") {
    (0 to 9).foreach { i =>
      assert(Await.result(targetSingleThreadedSlick.run(ddl.Children.filter(_.parentId === i).size.result), Duration.Inf) === 5)
      assert(Await.result(targetAkkaStreamsSlick.run(ddl.Children.filter(_.parentId === i).size.result), Duration.Inf) === 5)
    }
  }
}
