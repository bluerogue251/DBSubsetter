package e2e.pktypes

import e2e.AbstractMysqlEndToEndTest

class PkTypesMysqlTest extends AbstractMysqlEndToEndTest with PkTypesTestCases {
  override val originPort = 5570
  override val programArgs = Array(
    "--schemas", "pk_types",
    "--baseQuery", "pk_types.byte_pk_table ::: id = -128 ::: includeChildren",
    "--baseQuery", "pk_types.short_pk_table ::: id = -32768 ::: includeChildren",
    "--baseQuery", "pk_types.int_pk_table ::: id = -2147483648 ::: includeChildren",
    "--baseQuery", "pk_types.long_pk_table ::: id = -9223372036854775808 ::: includeChildren",
    "--baseQuery", "pk_types.uuid_pk_table ::: id = 'ae2c53e6-bef2-42cb-aaf7-3bdd58b0b645' ::: includeChildren",
    "--baseQuery", "pk_types.string_pk_table ::: id = \"two \" ::: includeChildren",
    "--baseQuery", "pk_types.referencing_table ::: id in (2, 4, 6, 8, 10, 12, 14, 16, 18, 21) ::: includeChildren"
  )
}
