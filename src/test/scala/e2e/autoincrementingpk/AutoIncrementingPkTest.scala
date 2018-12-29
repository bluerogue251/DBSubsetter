package e2e.autoincrementingpk

import org.scalatest.FunSuiteLike
import util.assertion.AssertionUtil

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait AutoIncrementingPkTest extends FunSuiteLike with AssertionUtil {
  protected val testName = "autoincrementing_pk"

  protected val profile: slick.jdbc.JdbcProfile

  protected def originSlick: slick.jdbc.JdbcBackend#DatabaseDef

  protected val ddl: DDL = new DDL(profile)

  import ddl.profile.api._

  protected def prepareOriginDDL(): Unit = {
    val ddlFuture = originSlick.run(ddl.schema.create)
    Await.ready(ddlFuture, Duration.Inf)
  }

  protected def prepareOriginDML(): Unit = {
    val dmlDbioAction = DML.dbioSeq(ddl)
    val dmlFuture = originSlick.run(dmlDbioAction)
    Await.ready(dmlFuture, Duration.Inf)
  }

  test("Correct records were included for main table and their primary keys values are correct") {
    assertCount(ddl.AutoincrementingPkTable, 10)
    // Tests that target databases don't over-write the primary key values
    // with the default values from their own primary key sequences.
    // The correct values for the primary keys are: 2, 4, 6, 8, 10, 12, 14, 16, 18, 20
    // If the target database overwrites PK values, we would see: 1, 2, 3, 4, 5, 6, etc.
    assertThat(ddl.AutoincrementingPkTable.map(_.id).sum.result, 110)
  }

  // Purposely including a second table because it is a known limitation of MS SQl Server
  // that only one table at a time can have auto increment disabled for inserts
  // I'm actually surprised it works, seeing as we never unset the IDENTITY_INSERT setting
  // for any tables after I've set them, so I would expect things to error out on any table except the first
  // But for some reason it does appear to work.
  test("Correct records were included for other table and their primary keys values are correct") {
    assertCount(ddl.OtherAutoincrementingPkTable, 10)
    assertThat(ddl.OtherAutoincrementingPkTable.map(_.id).sum.result, 110)
  }
}
