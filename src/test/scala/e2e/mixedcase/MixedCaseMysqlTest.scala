package e2e.mixedcase

import org.scalatest.FunSuite

class MixedCaseMysqlTest extends FunSuite {
  //  override val originPort = 5530
  //  override val programArgs = Array(
  //    "--schemas", "mIXED_case_DB",
  //    "--baseQuery", "mIXED_case_DB.mixed_CASE_table_1 ::: `ID` = 2 ::: includeChildren"
  //  )

  test("MySQL Mixed Case FKs") {
    // * MySQL schema introspection appears to have a bug whereby they don't properly capitalize column names of foreign key columns.
    //   -- Doesn't seem to be remedied by &useInformationSchema=true in DB URL so that the `DatabaseMetaDataUsingInfoSchema` class is used
    //   This seems to be a bug at the MySQL layer, not at the JDBC Driver layer
    //   Because the same issue is present in the command line program using the `show create table my_table` command
    pending
  }
}
