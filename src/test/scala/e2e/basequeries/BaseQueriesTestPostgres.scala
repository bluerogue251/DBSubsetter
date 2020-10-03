package e2e.basequeries

import e2e.PostgresSubsettingTest

class BaseQueriesTestPostgres extends PostgresSubsettingTest with BaseQueriesTest {
  // format: off
  
  override protected val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.base_table ::: true ::: excludeChildren"
  )
}
