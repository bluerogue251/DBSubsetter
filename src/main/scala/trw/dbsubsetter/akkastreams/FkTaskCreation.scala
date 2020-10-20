package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.workflow._

private[akkastreams] object FkTaskCreation {
  def flow(generator: FkTaskGenerator): Flow[PksAdded, IndexedSeq[ForeignKeyTask], NotUsed] = {
    Flow[PksAdded].map(generator.generateFrom)
  }
}
