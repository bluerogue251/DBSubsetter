package e2e

import util.db.Database

trait SlickSetup[T <: Database] extends SlickSetupDDL[T] with SlickSetupDML[T]
