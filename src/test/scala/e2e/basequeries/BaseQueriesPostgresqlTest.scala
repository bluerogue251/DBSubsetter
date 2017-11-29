package e2e.basequeries

import e2e.AbstractPostgresqlEndToEndTest

class BaseQueriesPostgresqlTest extends AbstractPostgresqlEndToEndTest with BaseQueriesTestCases {
  override val profile = slick.jdbc.PostgresProfile

  import profile.api._

  override val ddl = schema.create
  override val dml = new BaseQueriesDML(profile).dbioSeq
  override val dataSetName = "base_queries"
  override val originPort = 5513
  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.base_table ::: true ::: false"
  )
}
