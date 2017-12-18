package e2e.pgdatatypes

import e2e.AbstractPostgresqlEndToEndTest

import scala.sys.process._

class PgDataTypesPostgresqlTest extends AbstractPostgresqlEndToEndTest {
  override val dataSetName = "pg_data_types"
  override val originPort = 5500

  override val programArgs = Array(
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

  test("No error was thrown during subsetting -- TODO write some real tests") {
    pending
  }

  override protected def setupOriginDDL(): Unit = {
    s"psql --host 0.0.0.0 --port $originPort --user postgres $dataSetName --file ./src/test/scala/e2e/pgdatatypes/ddl.sql".!!
  }

  override protected def setupOriginDML(): Unit = {
    s"psql --host 0.0.0.0 --port $originPort --user postgres $dataSetName --file ./src/test/scala/e2e/pgdatatypes/dml.sql".!!
  }
}
