package trw.dbsubsetter.metrics

import io.prometheus.client.{Counter, Gauge, Histogram}

object Metrics {

  // format: off
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

  val offHeapQueueDurationBuckets: Array[Double] = Array(
    .0000001, .00000025, .0000005, .00000075,
    .000001, .0000025, .000005, .0000075,
    .00001, .000025, .00005, .000075,
    .0001, .0005,
    .001, .005,
    .01, .05
  )
  // format: on

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

  val PendingForeignKeyTasks: Gauge =
    Gauge
      .build()
      .name("PendingForeignKeyTasks")
      .help("n/a")
      .register()

  val ForeignKeyTaskEnqueueDuration: Histogram =
    Histogram
      .build()
      .name("ForeignKeyTaskEnqueueDuration")
      .help("n/a")
      .buckets(offHeapQueueDurationBuckets: _*)
      .register()

  val ForeignKeyTaskDequeueDuration: Histogram =
    Histogram
      .build()
      .name("ForeignKeyTaskDequeueDuration")
      .help("n/a")
      .buckets(offHeapQueueDurationBuckets: _*)
      .register()

  val PendingDataCopyRows: Gauge =
    Gauge
      .build()
      .name("PendingDataCopyRows")
      .help("n/a")
      .register()

  val DataCopyTaskEnqueueDuration: Histogram =
    Histogram
      .build()
      .name("DataCopyTaskEnqueueDuration")
      .help("n/a")
      .buckets(offHeapQueueDurationBuckets: _*)
      .register()

  val DataCopyTaskDequeueDuration: Histogram =
    Histogram
      .build()
      .name("DataCopyTaskDequeueDuration")
      .help("n/a")
      .buckets(offHeapQueueDurationBuckets: _*)
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
