package e2e.basequeries

import e2e.PostgresSubsettingTest

class BaseQueriesTestPostgreSQL extends PostgresSubsettingTest with BaseQueriesTest {

  override protected val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.base_table ::: true ::: excludeChildren"
  )
}
