package e2e

class SelfReferencingTest extends AbstractEndToEndTest {

  override val dataSetName = "self_referencing"

  override val originPort = 5520

  override val targetPort = 5521

  override val programArgs = Array(
    "--schemas", "public",
    "--originDbConnStr", "jdbc:postgresql://localhost:5520/self_referencing_origin?user=postgres",
    "--targetDbConnStr", targetConnString,
    "--baseQuery", "public.self_referencing_table ::: id in (1, 3, 13, 14, 15) ::: true",
    "--originDbParallelism", "1",
    "--targetDbParallelism", "1",
    "--singleThreadedDebugMode"
  )

  test("Correct self_referencing_table records were included") {
    assert(countTable("public", "self_referencing_table") === 10)
    assert(sumColumn("public", "self_referencing_table", "id") === 70)
  }
}
