package e2e

class SelfReferencingTest extends AbstractEndToEndTest {
  override val dataSetName = "self_referencing"
  override val originPort = 5520

  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.self_referencing_table ::: id in (1, 3, 13, 14, 15) ::: true"
  )

  test("Correct self_referencing_table records were included") {
    assertCount("public", "self_referencing_table", None, 10)
    assertSum("public", "self_referencing_table", "id", 70)
  }
}
