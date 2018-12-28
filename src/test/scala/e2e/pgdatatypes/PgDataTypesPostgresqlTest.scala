package e2e.pgdatatypes

import java.io.File

import e2e.AbstractPostgresqlEndToEndTest

import scala.sys.process._

class PgDataTypesPostgresqlTest extends AbstractPostgresqlEndToEndTest {
  override protected val testName = "pg_data_types"

  override protected val originPort = 5500

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
    "--baseQuery", "public.citext_table ::: true ::: includeChildren",
    // The following data types are unfortunately not working yet
    "--excludeColumns", "public.money_table(money)",
    "--excludeColumns", "public.enum_table(enum)",
    "--excludeColumns", "public.bit_string_table(bit_1, bit_5)"
  )

  test("No error was thrown during subsetting -- TODO add more detailed assertions") {
    pending
  }

  private val originPsqlCommand = s"docker exec -i ${containers.origin.name} psql --user postgres ${containers.origin.db.name}"

  override protected def prepareOriginDDL(): Unit = {
    val ddlFile = new File("./src/test/scala/e2e/pgdatatypes/ddl.sql")
    (ddlFile #> originPsqlCommand).!!
  }

  override protected def prepareOriginDML(): Unit = {
    val dmlFile = new File("./src/test/scala/e2e/pgdatatypes/dml.sql")
    (dmlFile #> originPsqlCommand).!!
  }
}
