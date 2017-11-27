package e2e

class BaseQueriesTest extends AbstractEndToEndTest {
  override val dataSetName = "base_queries"
  override val originPort = 5510

  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.base_table ::: true ::: false"
  )

  test("Correct base_table records were included") {
    assertCount("public", "base_table", None, 10)
    assertSum("public", "base_table", "id", 55)
  }

  test("Correct child_table records (none) were included") {
    assertCount("public", "child_table", None, 0)
  }
}
