package trw.dbsubsetter.akkastreams

import akka.actor.{Actor, Props}
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.primarykeystore.{PrimaryKeyStore, PrimaryKeyStoreFactory}
import trw.dbsubsetter.workflow._

// Only accessing the PrimaryKeyStore from inside an actor allows us to not need the PrimaryKeyStore to be threadsafe
private[this] class PkStoreActor(schemaInfo: SchemaInfo) extends Actor {
  private[this] val pkStore: PrimaryKeyStore = PrimaryKeyStoreFactory.getPrimaryKeyStore(schemaInfo)
  private[this] val pkStoreWorkflow = new PkStoreWorkflow(pkStore)

  override def receive: Receive = {
    // If it's a FetchParentTask, then we are being asked to pre-check to make sure we haven't done it already
    case fetchParentTask: FetchParentTask =>
      val alreadySeen: Boolean = pkStore.alreadySeen(fetchParentTask.foreignKey.toTable, fetchParentTask.value)
      val response: PkResult = if (alreadySeen) fetchParentTask else DuplicateTask
      sender() ! response
    // If it's an OriginDbResult, then we are being asked to add the new primary key values to the PkStore
    case req: OriginDbResult =>
      sender() ! pkStoreWorkflow.add(req)
    case other =>
      throw new RuntimeException(s"Cannot handle $other")
  }
}

object PkStoreActor {
  def props(schemaInfo: SchemaInfo): Props = {
    Props(new PkStoreActor(schemaInfo))
  }
}