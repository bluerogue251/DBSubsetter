package trw.dbsubsetter.config

import java.io.File

case class Config(
    originDbConnectionString: String,
    targetDbConnectionString: String,
    keyCalculationDbConnectionCount: Int,
    dataCopyDbConnectionCount: Int,
    tempfileStorageDirectoryOverride: Option[File],
    metricsPort: Option[Int]
)
