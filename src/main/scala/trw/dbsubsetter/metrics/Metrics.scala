package trw.dbsubsetter.metrics

import io.prometheus.client.{Gauge, Histogram}

object Metrics {
  val OriginDbSelectsHistogram: Histogram =
    Histogram
      .build()
      .name("OriginDbSelects")
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

  val PendingTasksGauge: Gauge =
    Gauge
    .build()
    .name("PendingTasks")
    .help("n/a")
    .register()
}
