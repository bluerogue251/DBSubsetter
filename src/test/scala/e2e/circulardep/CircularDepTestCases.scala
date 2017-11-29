package e2e.circulardep

import e2e.AbstractEndToEndTest

trait CircularDepTestCases extends AbstractEndToEndTest with CircularDepDDL {
  val dataSetName = "self_referencing"

  import profile.api._

  override lazy val ddl = schema.create
  override lazy val dml = new CircularDepDML(profile).dbioSeq

  test("Correct number of grandparents were included") {
    assertCount(Grandparents, 167)
  }

  test("All grandparents have correct number of parents") {
    (0 to 1000 by 6).foreach { i =>
      assert(targetDbSt.run(Parents.filter(_.grandparentId === i).size.result) === 10)
      assert(targetDbAs.run(Parents.filter(_.grandparentId === i).size.result) === 10)
    }
  }

  test("All parents have correct number of children") {
    (0 to 9).foreach { i =>
      assert(targetDbSt.run(Children.filter(_.parentId === i).size.result) === 5)
      assert(targetDbAs.run(Children.filter(_.parentId === i).size.result) === 5)
    }
  }
}
