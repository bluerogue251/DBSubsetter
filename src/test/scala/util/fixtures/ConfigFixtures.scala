package util.fixtures

import trw.dbsubsetter.config.Config

object ConfigFixtures {
  val emptyConfig: Config =
    Config(
      originDbConnectionString = "",
      targetDbConnectionString = "",
      keyCalculationDbConnectionCount = 1,
      dataCopyDbConnectionCount = 1,
      tempfileStorageDirectoryOverride = None,
      metricsPort = None
    )
}
