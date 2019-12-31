package trw.dbsubsetter.akkastreams

import akka.Done
import akka.stream.ClosedShape
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Balance, GraphDSL, Merge, RunnableGraph, Sink}
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.datacopyqueue.DataCopyQueue
import trw.dbsubsetter.db.{DbAccessFactory, SchemaInfo}
import trw.dbsubsetter.workflow._

import scala.concurrent.{ExecutionContext, Future}


// scalastyle:off
object DataCopyGraphFactory {
  def build(config: Config, schemaInfo: SchemaInfo, dbAccessFactory: DbAccessFactory, dataCopyQueue: DataCopyQueue)(implicit ec: ExecutionContext): RunnableGraph[Future[Done]] = RunnableGraph.fromGraph(GraphDSL.create(Sink.ignore) { implicit b =>sink =>
    val dataCopyBufferFlow = b.add(BufferFactory.dataCopyBuffer(dataCopyQueue).async)
    val balanceTargetDb = b.add(Balance[DataCopyTask](config.dataCopyDbConnectionCount, waitForAllDownstreams = true))
    val mergeTargetDbResults = b.add(Merge[Unit](config.dataCopyDbConnectionCount))

    // Dequeue Target DB insert requests
    dataCopyBufferFlow ~>
      balanceTargetDb

    // Process Target DB inserts in parallel
    for (_ <- 0 until config.dataCopyDbConnectionCount) {
      balanceTargetDb ~> TargetDb.insert(dbAccessFactory).async ~> mergeTargetDbResults
    }

    // And ignore all successful insert results
    mergeTargetDbResults.out ~> sink

    ClosedShape
  })
}
