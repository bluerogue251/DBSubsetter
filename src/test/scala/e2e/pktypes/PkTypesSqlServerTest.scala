//package e2e.pktypes
//
//import java.util.UUID
//
//import e2e.AbstractSqlServerEndToEndTest
//
//class PkTypesSqlServerTest extends AbstractSqlServerEndToEndTest with PkTypesTestCases {
//  override val originPort = 5576
//
//  override def expectedByteIds = super.expectedByteIds.filterNot(_ == -128)
//
//  override def expectedUUIDs: Seq[UUID] = super.expectedUUIDs.reverse
//
//  override def expectedReferencingTableIds: Seq[Int] = super.expectedReferencingTableIds.filterNot(_ == 1)
//
//  override val programArgs = Array(
//    "--schemas", "dbo",
//    "--baseQuery", "dbo.byte_pks ::: id = -128 ::: includeChildren",
//    "--baseQuery", "dbo.short_pks ::: id = -32768 ::: includeChildren",
//    "--baseQuery", "dbo.int_pks ::: id = -2147483648 ::: includeChildren",
//    "--baseQuery", "dbo.long_pks ::: id = -9223372036854775808 ::: includeChildren",
//    "--baseQuery", "dbo.uuid_pks ::: id = 'E6532CAE-F2BE-CB42-AAF7-3BDD58B0B645' ::: includeChildren",
//    "--baseQuery", "dbo.char_10_pks ::: id = 'two' ::: includeChildren",
//    "--baseQuery", "dbo.varchar_10_pks ::: id = 'six ' ::: includeChildren",
//    "--baseQuery", "dbo.referencing_table ::: id in (3, 4, 7, 8, 11, 12, 15, 16, 18, 21, 24) ::: includeChildren"
//  )
//}
