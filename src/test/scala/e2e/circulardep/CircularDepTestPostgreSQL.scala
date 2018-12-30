package e2e.circulardep

import e2e.AbstractPostgresqlEndToEndTest

class CircularDepTestPostgreSQL extends AbstractPostgresqlEndToEndTest with CircularDepTest {

  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.grandparents ::: id % 6 = 0 ::: includeChildren",
    "--skipPkStore", "public.children"
  )
}
