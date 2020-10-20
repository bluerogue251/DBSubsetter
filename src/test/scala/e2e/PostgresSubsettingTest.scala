package e2e

import util.db.PostgresDatabase

import scala.sys.process._

abstract class PostgresSubsettingTest extends PostgresEnabledTest with SubsettingTest[PostgresDatabase] {

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    postSubsetDdlSync(dbs.origin, dbs.targetSingleThreaded)
    postSubsetDdlSync(dbs.origin, dbs.target)
  }

  private def postSubsetDdlSync(origin: PostgresDatabase, target: PostgresDatabase): Unit = {
    val exportCommand =
      s"pg_dump --host ${origin.host} --port ${origin.port} --user postgres --section=post-data ${origin.name}"

    val importCommand =
      s"psql --host ${target.host} --port ${target.port} --user postgres ${target.name} -v ON_ERROR_STOP=1"

    (exportCommand #| importCommand).!!
  }
}
