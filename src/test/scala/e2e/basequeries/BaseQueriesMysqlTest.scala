package e2e.basequeries

import e2e.AbstractMysqlEndToEndTest

class BaseQueriesMysqlTest extends AbstractMysqlEndToEndTest with BaseQueriesTestCases {
  override val profile = slick.jdbc.MySQLProfile
  override val dataSetName = "base_queries"
  override val originPort = 5510
  override val programArgs = Array(
    "--schemas", "base_queries",
    "--baseQuery", "base_queries.base_table ::: true ::: false"
  )
}
