package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream._
import akka.stream.scaladsl.{BidiFlow, Flow}
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.workflow._

private[akkastreams] object PreTargetBufferFactory {

  def buildPreTargetBuffer(config: Config): Flow[PksAdded, PksAdded, NotUsed] = {
    var flow: Flow[PksAdded, PksAdded, NotUsed] =
      Flow[PksAdded].buffer(config.preTargetBufferSize, OverflowStrategy.backpressure)

    if (config.exposeMetrics) {
      flow = wrapWithInstrumentation(flow)
    }

    flow
  }

  private[this] def wrapWithInstrumentation(flow: Flow[PksAdded, PksAdded, NotUsed]): Flow[PksAdded, PksAdded, NotUsed] = {
    val instrumentFlowEntrance: PksAdded => PksAdded = pksEnteringBuffer => {
      ???
      pksEnteringBuffer
    }

    val instrumentFlowExit: PksAdded => PksAdded = pksExitingBuffer => {
      ???
      pksExitingBuffer
    }

    val wrapper: BidiFlow[PksAdded, PksAdded, PksAdded, PksAdded, NotUsed] =
      BidiFlow.fromFunctions(instrumentFlowEntrance, instrumentFlowExit)

    flow.join(wrapper)
  }
}
