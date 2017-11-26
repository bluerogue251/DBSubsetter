package e2e

class BaseQueriesTest extends AbstractEndToEndTest {
  override val dataSetName = "base_queries"
  override val originPort = 5510

  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.base_table ::: true ::: false"
  )

  test("Correct base_table records were included") {
    assert(countTable("public", "base_table") === 10)
    assert(sumColumn("public", "base_table", "id") === 55)
  }

  test("Correct child_table records (none) were included") {
    assert(countTable("public", "child_table") === 0)
  }
}
