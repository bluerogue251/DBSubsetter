package e2e.selfreferencing

import e2e.PostgresSubsettingTest

class SelfReferencingTestPostgres extends PostgresSubsettingTest with SelfReferencingTest {
  // format: off

  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.self_referencing_table ::: id in (1, 3, 13, 14, 15) ::: includeChildren"
  )
}
