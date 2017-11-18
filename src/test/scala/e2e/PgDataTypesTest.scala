package e2e

import java.sql.{Connection, DriverManager}

import org.scalatest.{BeforeAndAfterAll, FunSuite}
import trw.dbsubsetter.Application

import scala.sys.process._

class PgDataTypesTest extends FunSuite with BeforeAndAfterAll {
  val targetConnString = "jdbc:postgresql://localhost:5501/pg_data_types_target?user=postgres"
  var targetConn: Connection = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    "./util/pg_data_types/reset_origin_db.sh".!!
    "./util/pg_data_types/reset_target_db.sh".!!

    val args = Array(
      "--schemas", "public",
      "--originDbConnStr", "jdbc:postgresql://localhost:5500/pg_data_types_origin?user=postgres",
      "--targetDbConnStr", targetConnString,
      "--baseQuery", "public.arrays_table=true",
      "--baseQuery", "public.binary_table=true",
      //      "--baseQuery", "public.bit_string_table=true", // not working
      //      "--baseQuery", "public.enum_table=true", // not working
      "--baseQuery", "public.geometric_table=true",
      "--baseQuery", "public.hstore_table=true",
      "--baseQuery", "public.json_table=true",
      //      "--baseQuery", "public.money_table=true", // not working
      "--baseQuery", "public.network_address_table=true",
      "--baseQuery", "public.range_table=true",
      "--baseQuery", "public.text_search_table=true",
      "--baseQuery", "public.times_table=true",
      "--baseQuery", "public.xml_table=true",
      "--originDbParallelism", "1",
      "--targetDbParallelism", "1",
      "--singleThreadedDebugMode"
    )
    Application.main(args)

    "./util/pg_data_types/post_subset_target.sh".!!

    targetConn = DriverManager.getConnection(targetConnString)
    targetConn.setReadOnly(true)
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    targetConn.close()
  }

  test("No error was thrown during subsetting") {
    // Do nothing, just make sure an exception wasn't thrown
  }
}
