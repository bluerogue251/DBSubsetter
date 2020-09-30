package trw.dbsubsetter.config

case class CommandLineConfig(
    schemas: Seq[String] = Seq.empty,
    originDbConnectionString: String = "",
    targetDbConnectionString: String = "",
    baseQueries: Seq[String] = Seq.empty,
    keyCalculationDbConnectionCount: Int = 1,
    dataCopyDbConnectionCount: Int = 1,
    extraForeignKeys: Set[String] = Set.empty,
    extraPrimaryKeys: Set[String] = Set.empty,
    excludeTables: Set[String] = Set.empty,
    excludeColumns: Set[String] = Set.empty,
    tempfileStorageDirectoryOverride: Option[String] = None,
    runMode: RunMode = AkkaStreamsMode,
    metricsPort: Option[Int] = None
)

sealed trait RunMode
case object DebugMode extends RunMode
case object AkkaStreamsMode extends RunMode
