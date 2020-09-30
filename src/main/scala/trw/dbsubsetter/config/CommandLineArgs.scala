package trw.dbsubsetter.config

import java.io.File

case class CommandLineArgs(
    schemas: Set[String] = Set.empty,
    originDbConnectionString: String = "",
    targetDbConnectionString: String = "",
    baseQueries: Set[String] = Set.empty,
    keyCalculationDbConnectionCount: Int = 1,
    dataCopyDbConnectionCount: Int = 1,
    extraForeignKeys: Set[String] = Set.empty,
    extraPrimaryKeys: Set[String] = Set.empty,
    excludeTables: Set[String] = Set.empty,
    excludeColumns: Set[String] = Set.empty,
    tempfileStorageDirectoryOverride: Option[File] = None,
    runMode: RunMode = AkkaStreamsMode,
    metricsPort: Option[Int] = None
)

sealed trait RunMode
case object DebugMode extends RunMode
case object AkkaStreamsMode extends RunMode
