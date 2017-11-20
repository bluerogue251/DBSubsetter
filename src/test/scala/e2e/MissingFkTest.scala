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
      "--baseQuery", "public.table_1=id = 2",
      "--baseQuery", "public.table_a=id in (1, 2, 4, 5)",
      "--foreignKey", "public.table_2(table_1_id) ::: public.table_1(id)",
      "--foreignKey", "public.table_a(points_to_table_id) ::: public.table_b(id) ::: table_a.points_to_table_name='points_to_table_b'",
      "--foreignKey", "public.table_a(points_to_table_id) ::: public.table_c(id) ::: table_a.points_to_table_name='points_to_table_c'",
      "--foreignKey", "public.table_a(points_to_table_id) ::: public.table_d(id) ::: table_a.points_to_table_name='points_to_table_d'",
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
    assert(id1 === 47)
    assert(resultSet.next() === false)
  }

  test("Correct table_4 records were included") {
    val resultSet = targetConn.createStatement().executeQuery("select * from table_4 order by table_1_id asc, table_3_id_asc")
    resultSet.next()
    val id1 = resultSet.getInt("table_1_id")
    val id2 = resultSet.getInt("table_3_id")
    assert(id1 === 1)
    assert(id1 === 47)
    assert(resultSet.next() === false)
  }

  test("Correct table_5 records were included") {
    val resultSet = targetConn.createStatement().executeQuery("select * from table_5 order by id asc")
    resultSet.next()
    val id1 = resultSet.getInt("id")
    assert(id1 === 98)
    assert(resultSet.next() === false)
  }

  test("Correct table_a records were included") {
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
    val resultSet = targetConn.createStatement().executeQuery("select count(*) from table_c")
    resultSet.next()
    val count = resultSet.getInt(1)
    assert(count === 0)
  }

  test("Correct table_d records were included") {
    val resultSet = targetConn.createStatement().executeQuery("select * from table_d")
    resultSet.next()
    val id1 = resultSet.getInt("id")
    assert(id1 === 2)
    assert(resultSet.next() === false)
  }
}
