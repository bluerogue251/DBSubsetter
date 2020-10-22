package trw.dbsubsetter.config

import java.nio.file.Path

case class Config(
    originDbConnectionString: String,
    targetDbConnectionString: String,
    keyCalculationDbConnectionCount: Int,
    dataCopyDbConnectionCount: Int,
    storageDirectory: Option[Path],
    metricsPort: Option[Int]
)
