package e2e.circulardep

import e2e.PostgresSubsettingTest

class CircularDepTestPostgres extends PostgresSubsettingTest with CircularDepTest {
  // format: off
  
  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.grandparents ::: id % 6 = 0 ::: includeChildren"
  )
}
