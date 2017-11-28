package e2e

import e2e.ddl.Tables

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class MissingFkTest extends AbstractMysqlEndToEndTest {
  override val dataSetName = "missing_fk"
  override val originPort = 5490

  override val programArgs = Array(
    "--schemas", "missing_fk",
    "--baseQuery", "missing_fk.table_1 ::: id = 2 ::: true",
    "--foreignKey", "missing_fk.table_2(table_1_id) ::: missing_fk.table_1(id)",
    "--primaryKey", "missing_fk.table_4(table_1_id, table_3_id)"
  )

  test("Correct table_1 records were included") {
    assertCount("missing_fk", "table_1", None, 1)
    assertSum("missing_fk", "table_1", "id", 2)
  }

  test("Correct table_2 records were included") {
    // 1, 2
    assertCount("missing_fk", "table_2", None, 2)
    assertSum("missing_fk", "table_2", "id", 3)
  }

  test("Correct table_3 records were included") {
    // 45, 50
    assertCount("missing_fk", "table_3", None, 2)
    assertSum("missing_fk", "table_3", "id", 95)
  }

  test("Correct table_4 records were included") {
    // 2, 2
    assertCount("missing_fk", "table_4", None, 2)
    assertSum("missing_fk", "table_4", "table_1_id", 4)
    // 45, 50
    assertCount("missing_fk", "table_4", None, 2)
    assertSum("missing_fk", "table_4", "table_3_id", 95)
  }

  test("Correct table_5 records were included") {
    assertCount("missing_fk", "table_5", None, 1)
    assertSum("missing_fk", "table_5", "id", 99)
  }

  test("Correct table_a records were included") {
    pending
    val resultSet = targetSingleThreadedConn.createStatement().executeQuery("select * from table_a order by id asc")
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
    val resultSet = targetSingleThreadedConn.createStatement().executeQuery("select * from table_b")
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
    val resultSet = targetSingleThreadedConn.createStatement().executeQuery("select count(*) from table_c")
    resultSet.next()
    val count = resultSet.getInt(1)
    assert(count === 0)
  }

  test("Correct table_d records were included") {
    pending
    val resultSet = targetSingleThreadedConn.createStatement().executeQuery("select * from table_d order by id asc")
    resultSet.next()
    val id1 = resultSet.getInt("id")
    assert(id1 === 2)
    assert(resultSet.next() === false)
  }

  override def insertOriginDbData(): Unit = {
    import Tables._
    import slick.jdbc.MySQLProfile.api._
    val db = slick.jdbc.MySQLProfile.backend.Database.forURL(singleThreadedConfig.originDbConnectionString)

    val fut = db.run(
      DBIO.seq(
        Table1 ++= Seq(
          Table1Row(1),
          Table1Row(2)
        ),
        Table2 ++= Seq(
          Table2Row(1, 2),
          Table2Row(2, 2),
          Table2Row(3, 1)
        ),
        Table3 ++= Seq(
          Table3Row(45),
          Table3Row(46),
          Table3Row(47),
          Table3Row(48),
          Table3Row(49),
          Table3Row(50)
        ),
        Table4 ++= Seq(
          Table4Row(2, 45),
          Table4Row(1, 47),
          Table4Row(2, 50)
        ),
        Table5 ++= Seq(
          Table5Row(98, 1, 47),
          Table5Row(99, 2, 45)
        ),
        TableA ++= Seq(
          TableARow(1, "points_to_table_b", 1),
          TableARow(2, "points_to_table_b", 1),
          TableARow(3, "points_to_table_b", 2),
          TableARow(4, "points_to_table_d", 2),
          // edge case -- id does not exist in target table
          TableARow(5, "points_to_table_d", 30),
          // edge case -- Row 6 is NOT part of the subset, so row 1 of table_d should NOT be included
          // however, row #1 of table_b SHOULD be included.
          // This helps to test that table_b subsetting is not accidentally leaking over into table_d
          // This is based on a real bug that used to exist in our code
          TableARow(6, "points_to_table_d", 1)
        ),
        TableB ++= Seq(
          TableBRow(1),
          TableBRow(2),
          TableBRow(3)
        ),
        TableC ++= Seq(
          TableCRow(1),
          TableCRow(2),
          TableCRow(3)
        ),
        TableD ++= Seq(
          TableDRow(1),
          TableDRow(2),
          TableDRow(3)
        )
      )
    )
    Await.result(fut, Duration.Inf)
  }
}
