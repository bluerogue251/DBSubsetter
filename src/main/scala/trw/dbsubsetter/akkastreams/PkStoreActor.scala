package trw.dbsubsetter.akkastreams

import akka.actor.{Actor, Props}
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{PrimaryKeyValue, SchemaInfo}
import trw.dbsubsetter.primarykeystore.{PrimaryKeyStore, PrimaryKeyStoreFactory}
import trw.dbsubsetter.workflow._

// Only accessing the PrimaryKeyStore from inside this actor allows the PrimaryKeyStore to be non-threadsafe
private[this] class PkStoreActor(config: Config, schemaInfo: SchemaInfo) extends Actor {

  private[this] val pkStore: PrimaryKeyStore = PrimaryKeyStoreFactory.buildPrimaryKeyStore(config, schemaInfo)

  private[this] val pkStoreWorkflow = new PkStoreWorkflow(pkStore, schemaInfo)

  override def receive: Receive = {
    // If it's a FetchParentTask, then we are being asked to pre-check to make sure we haven't done it already
    case task @ FetchParentTask(fk, fkValueFromChild) =>
      val alreadySeen: Boolean =
        pkStore.alreadySeen(fk.toTable, new PrimaryKeyValue(fkValueFromChild.individualColumnValues))
      val response: PkQueryResult = if (alreadySeen) AlreadySeen else NotAlreadySeen(task)
      sender() ! response
    // If it's an OriginDbResult, then we are being asked to add the new primary key values to the PkStore
    case req: OriginDbResult =>
      sender() ! pkStoreWorkflow.add(req)
    case other =>
      throw new RuntimeException(s"Cannot handle $other")
  }
}

object PkStoreActor {
  def props(config: Config, schemaInfo: SchemaInfo): Props = {
    Props(new PkStoreActor(config, schemaInfo))
  }
}
