package e2e.selfreferencing

import e2e.PostgresEnabledTest

class SelfReferencingTestPostgreSQL extends PostgresEnabledTest with SelfReferencingTest {

  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.self_referencing_table ::: id in (1, 3, 13, 14, 15) ::: includeChildren"
  )
}
