package e2e

import java.sql.{Connection, DriverManager, ResultSet}

import org.scalatest.{BeforeAndAfterAll, FunSuite}
import trw.dbsubsetter.ApplicationSingleThreaded
import trw.dbsubsetter.db.Row

import scala.collection.mutable.ArrayBuffer
import scala.sys.process._

class CircularDepTest extends FunSuite with BeforeAndAfterAll {
  val targetConnString = "jdbc:postgresql://localhost:5481/circular_dep_target?user=postgres"
  var targetConn: Connection = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    "./util/circular_dep/reset_origin_db.sh".!!
    "./util/circular_dep/reset_target_db.sh".!!

    val args = Array(
      "--schemas", "public",
      "--originDbConnStr", "jdbc:postgresql://localhost:5480/circular_dep_origin?user=postgres",
      "--targetDbConnStr", targetConnString,
      "--baseQueries", "public.grandparents=id % 6 = 0",
      "--originDbParallelism", "1",
      "--targetDbParallelism", "1"
    )
    ApplicationSingleThreaded.main(args)

    "./util/circular_dep/post_subset_target.sh".!!

    targetConn = DriverManager.getConnection(targetConnString)
    targetConn.setReadOnly(true)
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    targetConn.close()
  }

  def jdbcResultToRows(res: ResultSet, numCols: Int): Vector[Row] = {
    val rows = ArrayBuffer.empty[Row]

    while (res.next()) {
      val row = new Array[AnyRef](numCols)
      (1 to numCols).foreach(i => row(i - 1) = res.getObject(i))
      rows += row
    }
    rows.toVector
  }

  test("Correct number of grandparents were included") {
    val resultSet = targetConn.createStatement().executeQuery("select count(*) from grandparents")
    resultSet.next()
    val grandparentCount = resultSet.getInt(1)
    assert(grandparentCount === 167)
  }

  test("Correct number of parents were included") {
    val resultSet = targetConn.createStatement().executeQuery("select count(*) from parents")
    resultSet.next()
    val parentCount = resultSet.getInt(1)
    assert(parentCount === 16700)
  }

  test("Correct number of children were included") {
    val resultSet = targetConn.createStatement().executeQuery("select count(*) from children")
    resultSet.next()
    val childrenCount = resultSet.getInt(1)
    assert(childrenCount === 167000)
  }

  test("Duplicate rows are not inserted") {
    val resultSet = targetConn.createStatement().executeQuery("select count(*) from parents where id = 0 and grandparent_id = 0")
    resultSet.next()
    val count = resultSet.getInt(1)
    assert(count === 1)
  }

  test("All grandparents have 100 parents") {
    (0 to 1000 by 6).foreach { i =>
      val parentResultSet = targetConn.createStatement().executeQuery(s"select count(*) from parents where grandparent_id = $i")
      parentResultSet.next()
      val parentCount = parentResultSet.getInt(1)
      assert(parentCount === 100, s"Grandparent id $i had $parentCount parents, expected 100")
    }
  }

  test("All parents have 10 children") {
    (0 to 9).foreach { i =>
      val childrenResultSet = targetConn.createStatement().executeQuery(s"select count(*) from children where parent_id = $i")
      childrenResultSet.next()
      val childCount = childrenResultSet.getInt(1)
      assert(childCount === 10, s"Parent id $i had $childCount children, expected 10")
    }
  }
}
