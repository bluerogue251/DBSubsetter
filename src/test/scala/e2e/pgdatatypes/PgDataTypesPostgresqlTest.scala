package e2e.pgdatatypes

import e2e.AbstractPostgresqlEndToEndTest

import scala.sys.process._

class PgDataTypesPostgresqlTest extends AbstractPostgresqlEndToEndTest {
  override val dataSetName = "pg_data_types"
  override val originPort = 5500

  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.arrays_table ::: true ::: true",
    "--baseQuery", "public.binary_table ::: true ::: true",
    "--baseQuery", "public.bit_string_table ::: true ::: true",
    "--baseQuery", "public.enum_table ::: true ::: true",
    "--baseQuery", "public.geometric_table ::: true ::: true",
    "--baseQuery", "public.hstore_table ::: true ::: true",
    "--baseQuery", "public.json_table ::: true ::: true",
    "--baseQuery", "public.money_table ::: true ::: true",
    "--baseQuery", "public.network_address_table ::: true ::: true",
    "--baseQuery", "public.range_table ::: true ::: true",
    "--baseQuery", "public.text_search_table ::: true ::: true",
    "--baseQuery", "public.times_table ::: true ::: true",
    "--baseQuery", "public.uuid_child_table ::: true ::: true",
    "--baseQuery", "public.xml_table ::: true ::: true",
    // The following data types are unfortunately not working yet
    "--excludeColumns", "public.money_table(money)",
    "--excludeColumns", "public.enum_table(enum)",
    "--excludeColumns", "public.bit_string_table(bit_1, bit_5)"
  )

  test("No error was thrown during subsetting") {
    // Do nothing, just make sure an exception wasn't thrown
    assert(1 === 1)
  }

  override protected def setupDDL(): Unit = {
    s"psql --host 0.0.0.0 --port $originPort --user postgres $dataSetName --file ./src/test/scala/e2e/pgdatatypes/ddl.sql".!!
  }

  override protected def setupDML(): Unit = {
    s"psql --host 0.0.0.0 --port $originPort --user postgres $dataSetName --file ./src/test/scala/e2e/pgdatatypes/dml.sql".!!
  }
}
