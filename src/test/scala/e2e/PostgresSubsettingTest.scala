package e2e

import util.db.PostgresDatabase

abstract class PostgresSubsettingTest extends PostgresEnabledTest with SubsettingTest[PostgresDatabase] {

}
