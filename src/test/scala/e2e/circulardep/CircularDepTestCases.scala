package e2e.circulardep

import e2e.{AbstractEndToEndTest, SlickSetup}
import util.assertion.AssertionUtil

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait CircularDepTestCases extends AbstractEndToEndTest with CircularDepDDL with SlickSetup with AssertionUtil {
  val dataSetName = "circular_dep"

  import profile.api._

  override lazy val ddl = schema.create
  override lazy val dml = new CircularDepDML(profile).dbioSeq

  test("Correct number of grandparents were included") {
    assertCount(Grandparents, 2)
  }

  test("All grandparents have correct number of parents") {
    (0 to 10 by 6).foreach { i =>
      assert(Await.result(targetDbSt.run(Parents.filter(_.grandparentId === i).size.result), Duration.Inf) === 10)
      assert(Await.result(targetAkkaStreamsSlick.run(Parents.filter(_.grandparentId === i).size.result), Duration.Inf) === 10)
    }
  }

  test("All parents have correct number of children") {
    (0 to 9).foreach { i =>
      assert(Await.result(targetDbSt.run(Children.filter(_.parentId === i).size.result), Duration.Inf) === 5)
      assert(Await.result(targetAkkaStreamsSlick.run(Children.filter(_.parentId === i).size.result), Duration.Inf) === 5)
    }
  }
}
