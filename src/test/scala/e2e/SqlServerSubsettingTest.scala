package e2e

import util.db.SqlServerDatabase

import scala.sys.process._

abstract class SqlServerSubsettingTest extends SqlServerEnabledTest with SubsettingTest[SqlServerDatabase] {

  override def beforeAll(): Unit = {
    super.beforeAll()
    s"./src/test/util/sqlserver_post_subset.sh ${dbs.origin.host} ${dbs.targetSingleThreaded.name}".!!
    s"./src/test/util/sqlserver_post_subset.sh ${dbs.origin.host} ${dbs.target.name}".!!
  }
}
