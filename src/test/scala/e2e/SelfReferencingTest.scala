package e2e

import java.sql.{Connection, DriverManager}

import org.scalatest.{BeforeAndAfterAll, FunSuite}
import trw.dbsubsetter.Application
import util.QueryUtil

import scala.sys.process._

class SelfReferencingTest extends FunSuite with BeforeAndAfterAll with QueryUtil {
  val targetConnString = "jdbc:postgresql://localhost:5521/self_referencing_target?user=postgres"
  var targetConn: Connection = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    "./util/self_referencing/reset_origin_db.sh".!!
    "./util/self_referencing/reset_target_db.sh".!!

    val args = Array(
      "--schemas", "public",
      "--originDbConnStr", "jdbc:postgresql://localhost:5520/self_referencing_origin?user=postgres",
      "--targetDbConnStr", targetConnString,
      "--baseQuery", "public.self_referencing_table ::: id in (1, 3, 13, 14, 15) ::: true",
      "--originDbParallelism", "1",
      "--targetDbParallelism", "1",
      "--singleThreadedDebugMode"
    )
    Application.main(args)

    "./util/self_referencing/post_subset_target.sh".!!

    targetConn = DriverManager.getConnection(targetConnString)
    targetConn.setReadOnly(true)
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    targetConn.close()
  }

  test("Correct self_referencing_table records were included") {
    assert(countTable("public", "self_referencing_table") === 10)
    assert(sumColumn("public", "self_referencing_table", "id") === 70)
  }
}
