package e2e

import util.Ports
import util.db._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.sys.process._
import scala.util.Properties

/**
  * A test which requires access to a running PostgreSQL database.
  */
abstract class PostgresEnabledTest extends DbEnabledTest[PostgresDatabase] {
  override protected val profile = slick.jdbc.PostgresProfile

  protected def testName: String

  override protected def createOriginDatabase(): Unit = {
    createDb(dbs.origin)
  }

  override protected def createTargetDatabases(): Unit = {
    createDb(dbs.target)
  }

  override protected def dbs: DatabaseSet[PostgresDatabase] = {
    val host = Properties.envOrElse("DB_SUBSETTER_POSTGRES_HOST", "localhost")
    val port = Ports.sharedPostgresPort

    val originDb = s"${testName}_origin"
    val targetDb = s"${testName}_target"

    new DatabaseSet(
      new PostgresDatabase(host, port, originDb),
      new PostgresDatabase(host, port, targetDb)
    )
  }

  override protected def prepareOriginSchemas(): Unit = {
    import slick.jdbc.PostgresProfile.api._

    additionalSchemas.foreach { additionalSchema =>
      val createSchemaStatement: DBIO[Unit] = DBIO.seq(
        sqlu"create schema #$additionalSchema"
      )
      Await.ready(originSlick.run(createSchemaStatement), Duration.Inf)
    }
  }

  override protected def prepareOriginDML(): Unit

  override protected def prepareTargetDDL(): Unit = {
    syncSchemaToTarget(dbs.origin, dbs.target)
  }

  private[this] def createDb(db: PostgresDatabase): Unit = {
    s"dropdb --host ${db.host} --port ${db.port} --user postgres --if-exists ${db.name}".!!
    s"createdb --host ${db.host} --port ${db.port} --user postgres ${db.name}".!!
  }

  private[this] def syncSchemaToTarget(origin: PostgresDatabase, target: PostgresDatabase): Unit = {
    val exportCommand =
      s"pg_dump --host ${origin.host} --port ${origin.port} --user postgres --section=pre-data ${origin.name}"

    val importCommand =
      s"psql --host ${target.host} --port ${target.port} --user postgres ${target.name}"

    (exportCommand #| importCommand).!!
  }
}
