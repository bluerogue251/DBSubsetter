package trw.dbsubsetter.keyingestion

import trw.dbsubsetter.OriginDbResult

trait KeyIngester {
  def ingest(originDbResult: OriginDbResult): Long
}
