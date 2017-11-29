package e2e.circulardep

import e2e.AbstractEndToEndTest

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait CircularDepTestCases extends AbstractEndToEndTest with CircularDepDDL {
  val dataSetName = "circular_dep"

  import profile.api._

  override lazy val ddl = schema.create
  override lazy val dml = new CircularDepDML(profile).dbioSeq

  test("Correct number of grandparents were included") {
    assertCount(Grandparents, 167)
  }

  test("All grandparents have correct number of parents") {
    (0 to 1000 by 6).foreach { i =>
      assert(Await.result(targetDbSt.run(Parents.filter(_.grandparentId === i).size.result), Duration.Inf) === 10)
      assert(Await.result(targetDbAs.run(Parents.filter(_.grandparentId === i).size.result), Duration.Inf) === 10)
    }
  }

  test("All parents have correct number of children") {
    (0 to 9).foreach { i =>
      assert(Await.result(targetDbSt.run(Children.filter(_.parentId === i).size.result), Duration.Inf) === 5)
      assert(Await.result(targetDbAs.run(Children.filter(_.parentId === i).size.result), Duration.Inf) === 5)
    }
  }
}
