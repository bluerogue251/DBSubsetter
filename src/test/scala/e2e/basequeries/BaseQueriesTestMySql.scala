package e2e.basequeries

import e2e.AbstractMysqlEndToEndTest

class BaseQueriesTestMySql extends AbstractMysqlEndToEndTest with BaseQueriesTest {

  override protected val programArgs = Array(
    "--schemas", "base_queries",
    "--baseQuery", "base_queries.base_table ::: true ::: excludeChildren"
  )
}