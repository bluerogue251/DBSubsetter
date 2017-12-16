package e2e.pktypes

import e2e.AbstractMysqlEndToEndTest

// Still need to test what happens with MySQL *unsigned* numerical primary keys
class PkTypesMysqlTest extends AbstractMysqlEndToEndTest with PkTypesTestCases {
  override val originPort = 5570

  override def expectedChar10Ids = Seq[String](" four", "two")

  override val programArgs = Array(
    "--schemas", "pk_types",
    "--baseQuery", "pk_types.byte_pks ::: id = -128 ::: includeChildren",
    "--baseQuery", "pk_types.short_pks ::: id = -32768 ::: includeChildren",
    "--baseQuery", "pk_types.int_pks ::: id = -2147483648 ::: includeChildren",
    "--baseQuery", "pk_types.long_pks ::: id = -9223372036854775808 ::: includeChildren",
    "--baseQuery", "pk_types.uuid_pks ::: id = X'ae2c53e6bef242cbaaf73bdd58b0b645' ::: includeChildren",
    "--baseQuery", "pk_types.char_10_pks ::: id = 'two' ::: includeChildren",
    "--baseQuery", "pk_types.varchar_10_pks ::: id = 'six ' ::: includeChildren",
    "--baseQuery", "pk_types.referencing_table ::: id in (3, 4, 7, 8, 11, 12, 15, 16, 18, 21, 24) ::: includeChildren"
  )
}
