package e2e.pgdatatypes

import java.io.File

import e2e.PostgresEnabledTest
import util.db.PostgreSQLDatabase

import scala.sys.process._

class PostgreSQLDataTypesTest extends PostgresEnabledTest {
  override protected val testName = "pg_data_types"

  override protected val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.arrays_table ::: true ::: includeChildren",
    "--baseQuery", "public.binary_table ::: true ::: includeChildren",
    "--baseQuery", "public.bit_string_table ::: true ::: includeChildren",
    "--baseQuery", "public.enum_table ::: true ::: includeChildren",
    "--baseQuery", "public.geometric_table ::: true ::: includeChildren",
    "--baseQuery", "public.hstore_table ::: true ::: includeChildren",
    "--baseQuery", "public.json_table ::: true ::: includeChildren",
    "--baseQuery", "public.money_table ::: true ::: includeChildren",
    "--baseQuery", "public.network_address_table ::: true ::: includeChildren",
    "--baseQuery", "public.range_table ::: true ::: includeChildren",
    "--baseQuery", "public.text_search_table ::: true ::: includeChildren",
    "--baseQuery", "public.times_table ::: true ::: includeChildren",
    "--baseQuery", "public.uuid_child_table ::: true ::: includeChildren",
    "--baseQuery", "public.xml_table ::: true ::: includeChildren",
    "--baseQuery", "public.citext_table ::: true ::: includeChildren"
  )

  test("No error was thrown during subsetting -- TODO add more detailed assertions") {
    pending
  }

  override protected def prepareOriginDDL(): Unit = {
    val ddlFile = new File("./src/test/scala/e2e/pgdatatypes/ddl.sql")
    (ddlFile #> originPsqlCommand).!!
  }

  override protected def prepareOriginDML(): Unit = {
    val dmlFile = new File("./src/test/scala/e2e/pgdatatypes/dml.sql")
    (dmlFile #> originPsqlCommand).!!
  }

  private def originPsqlCommand: String = {
    val originDb: PostgreSQLDatabase = dbs.origin
    s"psql --host ${originDb.host} --port ${originDb.port} --user postgres ${originDb.name}"
  }
}
