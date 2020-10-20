package trw.dbsubsetter.keyingestion

import trw.dbsubsetter.workflow.OriginDbResult

trait KeyIngester {
  def ingest(originDbResult: OriginDbResult): Long
}
