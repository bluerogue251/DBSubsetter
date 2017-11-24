package e2e

class PgDataTypesTest extends AbstractEndToEndTest {
  override val dataSetName = "pg_data_types"
  override val originPort = 5500
  override val targetPort = 5501

  override val programArgs = Array(
    "--schemas", "public",
    "--originDbConnStr", originConnString,
    "--targetDbConnStr", targetConnString,
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
    "--excludeColumns", "public.bit_string_table(bit_1, bit_5)",
    "--originDbParallelism", "1",
    "--targetDbParallelism", "1",
    "--singleThreadedDebugMode"
  )

  test("No error was thrown during subsetting") {
    // Do nothing, just make sure an exception wasn't thrown
  }
}
