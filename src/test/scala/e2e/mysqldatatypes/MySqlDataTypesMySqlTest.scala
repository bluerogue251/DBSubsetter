package e2e.mysqldatatypes

import java.io.File

import e2e.AbstractMysqlEndToEndTest
import util.assertion.AssertionUtil

import scala.sys.process._

class MySqlDataTypesMySqlTest extends AbstractMysqlEndToEndTest with AssertionUtil {
  override val testName = "mysql_data_types"
  override val originPort = 5580

  override val programArgs = Array(
    "--schemas", "mysql_data_types",
    "--baseQuery", "mysql_data_types.tinyints_signed ::: id in (127) ::: includeChildren",
    "--baseQuery", "mysql_data_types.tinyints_unsigned ::: id in (255) ::: includeChildren",
    "--baseQuery", "mysql_data_types.smallints_signed ::: id in (32767) ::: includeChildren",
    "--baseQuery", "mysql_data_types.smallints_unsigned ::: id in (65535) ::: includeChildren",
    "--baseQuery", "mysql_data_types.mediumints_signed ::: id in (8388607) ::: includeChildren",
    "--baseQuery", "mysql_data_types.mediumints_unsigned ::: id in (16777215) ::: includeChildren",
    "--baseQuery", "mysql_data_types.ints_signed ::: id in (2147483647) ::: includeChildren",
    "--baseQuery", "mysql_data_types.ints_unsigned ::: id in (4294967295) ::: includeChildren",
    "--baseQuery", "mysql_data_types.bigints_signed ::: id in (9223372036854775807) ::: includeChildren",
    "--baseQuery", "mysql_data_types.bigints_unsigned ::: id in (18446744073709551615) ::: includeChildren",
    "--baseQuery", "mysql_data_types.referencing_table ::: id in (1, 2, 6, 8, 12, 14, 18, 20, 24, 26, 30) ::: includeChildren"
  )

  test("No error was thrown during subsetting -- TODO add more detailed assertions") {
    pending
  }

  // TODO check list of ids so future developers can more easily see which IDs we expect
  test("referencing_table contained correct rows") {
    import profile.api._
    val sql = sql"checksum TABLE mysql_data_types.referencing_table EXTENDED".as[(String, String)]
    assertResult(sql, Seq(("mysql_data_types.referencing_table", "1211714113")))
  }

  private val originMySqlCommand = s"docker exec -i $originContainerName mysql --user root $originDbName"

  override protected def setupOriginDDL(): Unit = {
    val ddlFile = new File("./src/test/scala/e2e/mysqldatatypes/ddl.sql")
    (ddlFile #> originMySqlCommand).!!
  }

  override protected def setupOriginDML(): Unit = {
    val dmlFile = new File("./src/test/scala/e2e/mysqldatatypes/dml.sql")
    (dmlFile #> originMySqlCommand).!!
  }
}
