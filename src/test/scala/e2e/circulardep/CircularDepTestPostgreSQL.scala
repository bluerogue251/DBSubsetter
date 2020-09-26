package e2e.circulardep

import e2e.PostgresEnabledTest

class CircularDepTestPostgreSQL extends PostgresEnabledTest with CircularDepTest {

  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.grandparents ::: id % 6 = 0 ::: includeChildren"
  )
}
