package trw.dbsubsetter.config

import java.io.File

case class Config(
    originDbConnectionString: String = "",
    targetDbConnectionString: String = "",
    keyCalculationDbConnectionCount: Int = 1,
    dataCopyDbConnectionCount: Int = 1,
    tempfileStorageDirectoryOverride: Option[File] = None
)
