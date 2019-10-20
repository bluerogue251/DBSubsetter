package trw.dbsubsetter.metrics

import io.prometheus.client.{Counter, Gauge, Histogram}

object Metrics {
  val dbDurationPerStatementBuckets: Array[Double] = Array(
    .001, .0025, .005, .0075,
    .01, .025, .05, .075,
    .1, .25, .5, .75,
    1, 2.5, 5, 7.5,
    10, 25, 50, 75
  )

  val dbDurationPerRowBuckets: Array[Double] = Array(
    .00001, .000025, .000075,
    .0001, .00025, .0005, .00075,
    .001, .0025, .005, .0075,
    .01, .025, .05, .075,
    .1, .25, .5, .75
  )

  val taskQueueDurationBuckets: Array[Double] = Array(
    .0000001, .00000025, .0000005, .00000075,
    .000001, .0000025, .000005, .0000075,
    .00001, .000025, .00005, .000075,
    .0001, .0005,
    .001, .005,
    .01, .05
  )

  val OriginDbDurationPerStatement: Histogram =
    Histogram
      .build()
      .name("OriginDbDurationPerStatement")
      .help("n/a")
      .buckets(dbDurationPerStatementBuckets: _*)
      .register()

  val OriginDbRowsFetchedPerStatement: Histogram =
    Histogram
      .build()
      .name("OriginDbRowsFetchedPerStatement")
      .help("n/a")
      .exponentialBuckets(1, 2, 20)
      .register()

  val OriginDbDurationPerRow: Histogram =
    Histogram
      .build()
      .name("OriginDbDurationPerRow")
      .help("n/a")
      .buckets(dbDurationPerRowBuckets: _*)
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


  val TargetDbDurationPerStatement: Histogram =
    Histogram
      .build()
      .name("TargetDbDurationPerStatement")
      .help("n/a")
      .buckets(dbDurationPerStatementBuckets: _*)
      .register()

  val TargetDbRowsInsertedPerStatement: Histogram =
    Histogram
      .build()
      .name("TargetDbRowsInsertedPerStatement")
      .help("n/a")
      .exponentialBuckets(1, 2, 20)
      .register()

  val TargetDbDurationPerRow: Histogram =
    Histogram
      .build()
      .name("TargetDbDurationPerRow")
      .help("n/a")
      .buckets(dbDurationPerRowBuckets: _*)
      .register()

  val PendingTasksGauge: Gauge =
    Gauge
      .build()
      .name("PendingTasks")
      .help("n/a")
      .register()

  val TaskEnqueueDuration: Histogram =
    Histogram
      .build()
      .name("TaskEnqueueDuration")
      .help("n/a")
      .buckets(taskQueueDurationBuckets: _*)
      .register()

  val TaskDequeueDuration: Histogram =
    Histogram
      .build()
      .name("TaskDequeueDuration")
      .help("n/a")
      .buckets(taskQueueDurationBuckets: _*)
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
