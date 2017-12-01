package e2e.circulardep

import e2e.AbstractPostgresqlEndToEndTest

class CircularDepPostgresqlTest extends AbstractPostgresqlEndToEndTest with CircularDepTestCases {
  override val originPort = 5483
  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.grandparents ::: id % 6 = 0 ::: true"
  )
}
