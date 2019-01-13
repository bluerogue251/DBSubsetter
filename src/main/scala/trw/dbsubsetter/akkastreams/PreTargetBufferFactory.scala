package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream._
import akka.stream.scaladsl.{BidiFlow, Flow}
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.metrics.Metrics
import trw.dbsubsetter.workflow._

private[akkastreams] object PreTargetBufferFactory {

  def buildPreTargetBuffer(config: Config): Flow[PksAdded, PksAdded, NotUsed] = {
    var flow: Flow[PksAdded, PksAdded, NotUsed] =
      Flow[PksAdded].buffer(config.preTargetBufferSize, OverflowStrategy.backpressure)

    if (config.exposeMetrics) {
      Metrics.PreTargetBufferMaxSizeGauge.set(config.preTargetBufferSize)
      flow = wrapWithInstrumentation(flow)
    }

    flow
  }

  private[this] def wrapWithInstrumentation(flow: Flow[PksAdded, PksAdded, NotUsed]): Flow[PksAdded, PksAdded, NotUsed] = {
    val instrumentEntrance: PksAdded => PksAdded = pksEnteringBuffer => {
      Metrics.PreTargetBufferSizeGauge.inc()
      Metrics.PreTargetBufferRowsGauge.inc(pksEnteringBuffer.rowsNeedingParentTasks.size)
      pksEnteringBuffer
    }

    val instrumentExit: PksAdded => PksAdded = pksExitingBuffer => {
      Metrics.PreTargetBufferSizeGauge.dec()
      Metrics.PreTargetBufferRowsGauge.dec(pksExitingBuffer.rowsNeedingParentTasks.size)
      pksExitingBuffer
    }

    val wrapper: BidiFlow[PksAdded, PksAdded, PksAdded, PksAdded, NotUsed] =
      BidiFlow.fromFunctions(instrumentEntrance, instrumentExit)

    wrapper.join(flow)
  }
}
