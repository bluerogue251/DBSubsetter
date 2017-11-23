package trw.dbsubsetter

import akka.Done
import akka.actor.{Actor, ActorRef, ActorSystem, Props, Status}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import trw.dbsubsetter.akkastreams.{PrimaryKeyStore, SubsettingSource}
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
    val monitor: ActorRef = system.actorOf(Props[Monitor])
    val pkStore: ActorRef = system.actorOf(Props(classOf[PrimaryKeyStore], schemaInfo.pkOrdinalsByTable))

    SubsettingSource
      .source(config, schemaInfo, baseQueries, monitor)
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

class Monitor extends Actor {
  override def receive: Receive = {
    case (name: String, Done) => println(s"$name completed")
    case Status.Failure(e) => println(s"Failed: ${e.getMessage}")
  }
}