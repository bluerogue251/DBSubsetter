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

  val OutstandingTasksGauge: Gauge =
    Gauge
    .build()
    .name("OustandingTasks")
    .help("n/a")
    .register()
}
