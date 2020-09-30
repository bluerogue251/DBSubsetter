package trw.dbsubsetter.config

import java.io.File

case class Config(
    originDbConnectionString: String = "",
    targetDbConnectionString: String = "",
    keyCalculationDbConnectionCount: Int = 1,
    dataCopyDbConnectionCount: Int = 1,
    tempfileStorageDirectoryOverride: Option[File] = None,
    runMode: RunMode = AkkaStreamsMode,
    metricsPort: Option[Int] = None
)

sealed trait RunMode
case object DebugMode extends RunMode
case object AkkaStreamsMode extends RunMode
