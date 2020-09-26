package e2e.circulardep

import e2e.PostgresSubsettingTest

class CircularDepTestPostgreSQL extends PostgresSubsettingTest with CircularDepTest {

  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.grandparents ::: id % 6 = 0 ::: includeChildren"
  )
}
