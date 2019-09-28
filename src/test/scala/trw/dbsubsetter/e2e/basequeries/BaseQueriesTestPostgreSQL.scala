package trw.dbsubsetter.e2e.basequeries

import trw.dbsubsetter.e2e.AbstractPostgresqlEndToEndTest

class BaseQueriesTestPostgreSQL extends AbstractPostgresqlEndToEndTest with BaseQueriesTest {

  override protected val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.base_table ::: true ::: excludeChildren"
  )
}
