package e2e

class CircularDepTest extends AbstractEndToEndTest {
  override val dataSetName = "circular_dep"
  override val originPort = 5480
  override val targetPort = 5481

  override val programArgs = Array(
    "--schemas", "public",
    "--originDbConnStr", originConnString,
    "--targetDbConnStr", targetConnString,
    "--baseQuery", "public.grandparents ::: id % 6 = 0 ::: true",
    "--originDbParallelism", "1",
    "--targetDbParallelism", "1",
    "--singleThreadedDebugMode"
  )

  test("Correct number of grandparents were included") {
    val resultSet = targetConn.createStatement().executeQuery("select count(*) from grandparents")
    resultSet.next()
    val grandparentCount = resultSet.getInt(1)
    assert(grandparentCount === 167)
  }

  test("All grandparents have 100 parents") {
    (0 to 1000 by 6).foreach { i =>
      val parentResultSet = targetConn.createStatement().executeQuery(s"select count(*) from parents where grandparent_id = $i")
      parentResultSet.next()
      val parentCount = parentResultSet.getInt(1)
      assert(parentCount === 100, s"Grandparent id $i had $parentCount parents, expected 100")
    }
  }

  test("All parents have 10 children") {
    (0 to 9).foreach { i =>
      val childrenResultSet = targetConn.createStatement().executeQuery(s"select count(*) from children where parent_id = $i")
      childrenResultSet.next()
      val childCount = childrenResultSet.getInt(1)
      assert(childCount === 10, s"Parent id $i had $childCount children, expected 10")
    }
  }
}
