package trw.dbsubsetter.metrics

import io.prometheus.client.{Counter, Gauge, Histogram}

object Metrics {
  val dbStatementDurationBuckets: Array[Double] = Array(
    .001, .0025, .005, .0075,
    .01, .025, .05, .075,
    .1, .25, .5, .75,
    1, 2.5, 5, 7.5,
    10, 25, 50, 75
  )

  val OriginDbRowsFetchedPerStatement: Histogram =
    Histogram
      .build()
      .name("OriginDbRowsFetchedPerStatement")
      .help("n/a")
      .buckets(dbStatementDurationBuckets: _*)
      .register()

  val OriginDbDurationPerStatement: Histogram =
    Histogram
      .build()
      .name("OriginDbDurationPerStatement")
      .help("n/a")
      .exponentialBuckets(1, 2, 20)
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

  val TargetDbRowsInsertedPerStatement: Histogram =
    Histogram
      .build()
      .name("TargetDbRowsInsertedPerStatement")
      .help("n/a")
      .exponentialBuckets(1, 2, 20)
      .register()

  val TargetDbDurationPerStatement: Histogram =
    Histogram
      .build()
      .name("TargetDbDurationPerStatement")
      .help("n/a")
      .buckets(dbStatementDurationBuckets: _*)
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
