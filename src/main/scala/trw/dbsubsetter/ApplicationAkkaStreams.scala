package trw.dbsubsetter

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import trw.dbsubsetter.akkastreams.{PkStore, Subsetting}
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.util.Util
import trw.dbsubsetter.workflow._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object ApplicationAkkaStreams {
  def run(config: Config, schemaInfo: SchemaInfo, baseQueries: List[SqlStrQuery], startingTime: Long): Unit = {
    implicit val system: ActorSystem = ActorSystem("DbSubsetter")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext = system.dispatcher
    val pkStore: ActorRef = system.actorOf(Props(classOf[PkStore], schemaInfo.pkOrdinalsByTable))

    Subsetting
      .source(config, schemaInfo, baseQueries, pkStore)
      .runWith(Sink.ignore)
      .onComplete { result =>
        system.terminate()
        result match {
          case Success(_) => Util.printRuntime(startingTime)
          case Failure(e) => throw e
        }
      }
  }
}