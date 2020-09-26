package e2e.circulardep

import e2e.MySqlSubsettingTest

class CircularDepTestMySql extends MySqlSubsettingTest with CircularDepTest {

  override val programArgs = Array(
    "--schemas", "circular_dep",
    "--baseQuery", "circular_dep.grandparents ::: id % 6 = 0 ::: includeChildren"
  )
}
