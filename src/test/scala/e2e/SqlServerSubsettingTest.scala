package e2e

import util.db.SqlServerDatabase

abstract class SqlServerSubsettingTest extends SqlServerEnabledTest with SubsettingTest[SqlServerDatabase] {

}
