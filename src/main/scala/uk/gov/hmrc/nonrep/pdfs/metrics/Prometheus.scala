/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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