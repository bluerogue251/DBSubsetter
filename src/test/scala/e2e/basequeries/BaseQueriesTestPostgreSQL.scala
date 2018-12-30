package e2e.basequeries

import e2e.AbstractPostgresqlEndToEndTest

class BaseQueriesTestPostgreSQL extends AbstractPostgresqlEndToEndTest with BaseQueriesTest {

  override protected val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.base_table ::: true ::: excludeChildren"
  )
}
