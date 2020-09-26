package e2e.selfreferencing

import e2e.PostgresSubsettingTest

class SelfReferencingTestPostgreSQL extends PostgresSubsettingTest with SelfReferencingTest {

  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.self_referencing_table ::: id in (1, 3, 13, 14, 15) ::: includeChildren"
  )
}
