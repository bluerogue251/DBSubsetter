package e2e.selfreferencing

import e2e.MySqlEnabledTest

class SelfReferencingTestMySql extends MySqlEnabledTest with SelfReferencingTest {

  override val programArgs = Array(
    "--schemas", "self_referencing",
    "--baseQuery", "self_referencing.self_referencing_table ::: id in (1, 3, 13, 14, 15) ::: includeChildren"
  )
}
