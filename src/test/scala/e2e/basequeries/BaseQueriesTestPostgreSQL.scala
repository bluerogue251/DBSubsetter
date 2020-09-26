package e2e.basequeries

import e2e.PostgresEnabledTest

class BaseQueriesTestPostgreSQL extends PostgresEnabledTest with BaseQueriesTest {

  override protected val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.base_table ::: true ::: excludeChildren"
  )
}
