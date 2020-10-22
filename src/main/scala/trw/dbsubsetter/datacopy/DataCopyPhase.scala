package trw.dbsubsetter.datacopy

import trw.dbsubsetter.db.{DbAccessFactory, SchemaInfo}

trait DataCopyPhase {
  def runPhase()
}

object DataCopyPhase {
  def from(
      parallelism: Int,
      dbAccessFactory: DbAccessFactory,
      schemaInfo: SchemaInfo,
      dataCopyQueue: DataCopyQueue
  ): DataCopyPhase = {
    val copiers: Seq[DataCopier] = (1 to parallelism).map(_ => DataCopier.from(dbAccessFactory, schemaInfo))
    new DataCopyPhaseImpl(dataCopyQueue, copiers)
  }
}
