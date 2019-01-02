package trw.dbsubsetter.metrics

import io.prometheus.client.Histogram

object Metrics {


  val OriginDbExtractJdbcResults: Histogram = Histogram
    .build()
    .name("OriginDbExtractJdbcResults")
    .help("n/a")
    .register()
}
