package e2e

class CircularDepTest extends AbstractEndToEndTest {
  override val dataSetName = "circular_dep"
  override val originPort = 5480

  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.grandparents ::: id % 6 = 0 ::: true"
  )

  test("Correct number of grandparents were included") {
    assertCount("public", "grandparents", None, 167)
  }

  test("All grandparents have correct number of parents") {
    (0 to 1000 by 6).foreach { i =>
      assertCount("public", "parents", Some(s"grandparent_id = $i"), 10)
    }
  }

  test("All parents have correct number of children") {
    (0 to 9).foreach { i =>
      assertCount("public", "children", Some(s"parent_id = $i"), 5)
    }
  }
}
