package trw.dbsubsetter.metrics

import io.prometheus.client.Histogram

object Metrics {
  val OriginDbSelectsHistogram: Histogram =
    Histogram
      .build()
      .name("OriginDbSelects")
      .help("n/a")
      .register()

  val TargetDbInsertsHistogram: Histogram =
    Histogram
      .build()
      .name("TargetDbInserts")
      .help("n/a")
      .register()
}
