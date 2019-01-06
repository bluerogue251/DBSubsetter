package trw.dbsubsetter.akkastreams

import akka.actor.{Actor, Props}
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.primarykeystore.{PrimaryKeyStore, PrimaryKeyStoreFactory}
import trw.dbsubsetter.workflow._

// Only accessing the PrimaryKeyStore from inside this actor allows the PrimaryKeyStore to be non-threadsafe
private[this] class PkStoreActor(schemaInfo: SchemaInfo) extends Actor {

  private[this] val pkStore: PrimaryKeyStore = PrimaryKeyStoreFactory.buildPrimaryKeyStore(schemaInfo)

  private[this] val pkStoreWorkflow = new PkStoreWorkflow(pkStore, schemaInfo)

  override def receive: Receive = {
    // If it's a FkTask, then we are being asked to pre-check to make sure we haven't done it already
    case task @ FetchParentTask(table, _, fkValue) =>
      val alreadySeen: Boolean = pkStore.alreadySeen(table, fkValue)
      val response: PkQueryResult = if (alreadySeen) AlreadySeen else NotAlreadySeen(task)
      sender() ! response
    // If it's an OriginDbResult, then we are being asked to add the new primary key values to the PkStore
    case req: OriginDbResult =>
      sender() ! pkStoreWorkflow.add(req)
  }
}

object PkStoreActor {
  def props(schemaInfo: SchemaInfo): Props = {
    Props(new PkStoreActor(schemaInfo))
  }
}