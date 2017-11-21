package e2e

import java.sql.{Connection, DriverManager}

import org.scalatest.{BeforeAndAfterAll, FunSuite}
import trw.dbsubsetter.Application

import scala.sys.process._

class MissingFkTest extends FunSuite with BeforeAndAfterAll {
  val targetConnString = "jdbc:postgresql://localhost:5491/missing_fk_target?user=postgres"
  var targetConn: Connection = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    "./util/missing_fk/reset_origin_db.sh".!!
    "./util/missing_fk/reset_target_db.sh".!!

    val args = Array(
      "--schemas", "public",
      "--originDbConnStr", "jdbc:postgresql://localhost:5490/missing_fk_origin?user=postgres",
      "--targetDbConnStr", targetConnString,
      "--baseQuery", "public.table_1 ::: id = 2 ::: true",
      "--foreignKey", "public.table_2(table_1_id) ::: public.table_1(id)",
      "--primaryKey", "public.table_4(table_1_id, table_3_id)",
      "--originDbParallelism", "1",
      "--targetDbParallelism", "1",
      "--singleThreadedDebugMode"
    )
    Application.main(args)

    "./util/missing_fk/post_subset_target.sh".!!

    targetConn = DriverManager.getConnection(targetConnString)
    targetConn.setReadOnly(true)
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    targetConn.close()
  }

  test("Correct table_1 records were included") {
    val resultSet = targetConn.createStatement().executeQuery("select * from table_1")
    resultSet.next()
    val id = resultSet.getInt("id")
    assert(id === 2)
    assert(resultSet.next() === false)
  }

  test("Correct table_2 records were included") {
    val resultSet = targetConn.createStatement().executeQuery("select * from table_2 order by id asc")
    resultSet.next()
    val id1 = resultSet.getInt("id")
    assert(id1 === 1)
    resultSet.next()
    val id2 = resultSet.getInt("id")
    assert(id2 === 2)
    assert(resultSet.next() === false)
  }

  test("Correct table_3 records were included") {
    val resultSet = targetConn.createStatement().executeQuery("select * from table_3 order by id asc")
    resultSet.next()
    val id1 = resultSet.getInt("id")
    assert(id1 === 45)
    resultSet.next()
    val id2 = resultSet.getInt("id")
    assert(id2 === 50)
    assert(resultSet.next() === false)
  }

  test("Correct table_4 records were included") {
    val resultSet = targetConn.createStatement().executeQuery("select * from table_4 order by table_1_id asc, table_3_id asc")
    resultSet.next()
    val id1 = resultSet.getInt("table_1_id")
    val id2 = resultSet.getInt("table_3_id")
    assert(id1 === 2)
    assert(id2 === 45)
    resultSet.next()
    val id3 = resultSet.getInt("table_1_id")
    val id4 = resultSet.getInt("table_3_id")
    assert(id3 === 2)
    assert(id4 === 50)
    assert(resultSet.next() === false)
  }

  test("Correct table_5 records were included") {
    val resultSet = targetConn.createStatement().executeQuery("select * from table_5 order by id asc")
    resultSet.next()
    val id1 = resultSet.getInt("id")
    assert(id1 === 99)
    assert(resultSet.next() === false)
  }

  test("Correct table_a records were included") {
    pending
    val resultSet = targetConn.createStatement().executeQuery("select * from table_a order by id asc")
    resultSet.next()
    val id1 = resultSet.getInt("id")
    assert(id1 === 1)
    resultSet.next()
    val id2 = resultSet.getInt("id")
    assert(id2 === 2)
    resultSet.next()
    val id4 = resultSet.getInt("id")
    assert(id4 === 4)
    resultSet.next()
    val id5 = resultSet.getInt("id")
    assert(id5 === 5)
    assert(resultSet.next() === false)
  }

  test("Correct table_b records were included") {
    pending
    val resultSet = targetConn.createStatement().executeQuery("select * from table_b")
    resultSet.next()
    val id1 = resultSet.getInt("id")
    assert(id1 === 1)
    resultSet.next()
    val id2 = resultSet.getInt("id")
    assert(id2 === 2)
    assert(resultSet.next() === false)
  }

  test("Correct table_c records were included") {
    pending
    val resultSet = targetConn.createStatement().executeQuery("select count(*) from table_c")
    resultSet.next()
    val count = resultSet.getInt(1)
    assert(count === 0)
  }

  test("Correct table_d records were included") {
    pending
    val resultSet = targetConn.createStatement().executeQuery("select * from table_d order by id asc")
    resultSet.next()
    val id1 = resultSet.getInt("id")
    assert(id1 === 2)
    assert(resultSet.next() === false)
  }
}
