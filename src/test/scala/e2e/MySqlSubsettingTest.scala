package e2e

import util.db.MySqlDatabase

abstract class MySqlSubsettingTest extends MySqlEnabledTest with SubsettingTest[MySqlDatabase] {

}
