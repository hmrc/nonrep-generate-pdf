package uk.gov.hmrc.nonrep.pdfs.metrics

import fr.davit.akka.http.metrics.prometheus.{Buckets, PrometheusRegistry, PrometheusSettings, Quantiles}
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.hotspot.DefaultExports

object Prometheus {

  val prometheus = CollectorRegistry.defaultRegistry
  val settings = PrometheusSettings.default.
    withNamespace("generate_pdf").
    withIncludePathDimension(true).
    withIncludeMethodDimension(true).
    withIncludeStatusDimension(true).
    withDurationConfig(Buckets(.1, .2, .3, .5, .8, 1, 1.5, 2, 2.5, 3, 5, 8, 13, 21)).
    withReceivedBytesConfig(Quantiles(0.5, 0.75, 0.9, 0.95, 0.99)).
    withSentBytesConfig(PrometheusSettings.DefaultQuantiles).
    withDefineError(_.status.isFailure)
  DefaultExports.initialize()
  val registry = PrometheusRegistry(prometheus, settings)

}