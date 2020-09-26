package e2e.circulardep

import e2e.MySqlEnabledTest

class CircularDepTestMySql extends MySqlEnabledTest with CircularDepTest {

  override val programArgs = Array(
    "--schemas", "circular_dep",
    "--baseQuery", "circular_dep.grandparents ::: id % 6 = 0 ::: includeChildren"
  )
}
