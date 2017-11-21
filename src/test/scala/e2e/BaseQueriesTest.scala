package e2e

import java.sql.{Connection, DriverManager}

import org.scalatest.{BeforeAndAfterAll, FunSuite}
import trw.dbsubsetter.Application
import util.QueryUtil

import scala.sys.process._

class BaseQueriesTest extends FunSuite with BeforeAndAfterAll with QueryUtil {
  val targetConnString = "jdbc:postgresql://localhost:5511/base_queries_target?user=postgres"
  var targetConn: Connection = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    "./util/base_queries/reset_origin_db.sh".!!
    "./util/base_queries/reset_target_db.sh".!!

    val args = Array(
      "--schemas", "public",
      "--originDbConnStr", "jdbc:postgresql://localhost:5510/base_queries_origin?user=postgres",
      "--targetDbConnStr", targetConnString,
      "--baseQuery", "public.base_table ::: true ::: false",
      "--originDbParallelism", "1",
      "--targetDbParallelism", "1",
      "--singleThreadedDebugMode"
    )
    Application.main(args)

    "./util/base_queries/post_subset_target.sh".!!

    targetConn = DriverManager.getConnection(targetConnString)
    targetConn.setReadOnly(true)
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    targetConn.close()
  }

  test("Correct base_table records were included") {
    assert(countTable("public", "base_table") === 10)
    assert(sumColumn("public", "base_table", "id") === 55)
  }

  test("Correct child_table records (none) were included") {
    assert(countTable("public", "child_table") === 0)
  }
}
