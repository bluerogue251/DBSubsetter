package trw.dbsubsetter

import akka.Done
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import trw.dbsubsetter.akkastreams.{PkStoreActor, Subsetting}
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{DbAccessFactory, SchemaInfo}
import trw.dbsubsetter.workflow._

import scala.concurrent.{ExecutionContext, Future}

object ApplicationAkkaStreams {
  def run(config: Config, schemaInfo: SchemaInfo, baseQueries: Vector[BaseQuery]): Future[Done] = {
    implicit val system: ActorSystem = ActorSystem("DbSubsetter")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext = system.dispatcher

    val pkStore: ActorRef = system.actorOf(PkStoreActor.props(schemaInfo))
    val dbAccessFactory: DbAccessFactory = new DbAccessFactory(config, schemaInfo)

    Subsetting
      .source(config, schemaInfo, baseQueries, pkStore, dbAccessFactory)
      .runWith(Sink.ignore)
      .map { success =>
        dbAccessFactory.closeAllConnections()
        system.terminate()
        success
      }
  }
}