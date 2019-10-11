package trw.dbsubsetter.metrics

import io.prometheus.client.{Counter, Gauge, Histogram}

object Metrics {
  val OriginDbSelectsHistogram: Histogram =
    Histogram
      .build()
      .name("OriginDbSelects")
      .help("n/a")
      .register()

  val OriginDbRowsFetched: Counter =
    Counter
      .build()
      .name("OriginDbRowsFetched")
      .help("n/a")
      .register()

  val DuplicateOriginDbRowsDiscarded: Counter =
    Counter
      .build()
      .name("DuplicateOriginDbRowsDiscarded")
      .help("n/a")
      .register()

  val JdbcResultConverterHistogram: Histogram =
    Histogram
      .build()
      .name("OriginDbJdbcResultToObjectMapper")
      .help("n/a")
      .register()

  val TargetDbInsertsHistogram: Histogram =
    Histogram
      .build()
      .name("TargetDbInserts")
      .help("n/a")
      .register()

  val TargetDbRowsInserted: Counter =
    Counter
      .build()
      .name("TargetDbRowsInserted")
      .help("n/a")
      .register()

  val PendingTasksGauge: Gauge =
    Gauge
      .build()
      .name("PendingTasks")
      .help("n/a")
      .register()

  val PreTargetBufferSizeGauge: Gauge =
    Gauge
      .build()
      .name("PreTargetBufferSize")
      .help("n/a")
      .register()

  val PreTargetBufferRowsGauge: Gauge =
    Gauge
      .build()
      .name("PreTargetBufferRows")
      .help("n/a")
      .register()

  val DuplicateFkTasksDiscarded: Counter =
    Counter
      .build()
      .name("DuplicateFkTasksDiscarded")
      .help("n/a")
      .register()

  val PkStoreMarkSeenHistogram: Histogram =
    Histogram
      .build()
      .name("PrimaryKeyStoreMarkSeen")
      .help("n/a")
      .register()

  val PkStoreMarkSeenWithChildrenHistogram: Histogram =
    Histogram
      .build()
      .name("PrimaryKeyStoreMarkSeenWithChildren")
      .help("n/a")
      .register()

  val PkStoreQueryAlreadySeenHistogram: Histogram =
    Histogram
      .build()
      .name("PrimaryKeyStoreQueryAlreadySeen")
      .help("n/a")
      .register()
}
