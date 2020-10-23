package trw.dbsubsetter.fkcalc

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{CountDownLatch, ExecutorService, Executors}

import trw.dbsubsetter.db.{PrimaryKeyValue, Table}
import trw.dbsubsetter.fktaskqueue.ForeignKeyTaskQueue
import trw.dbsubsetter.keyingestion.KeyIngester
import trw.dbsubsetter.pkstore.PkStoreWorkflow

final class ForeignKeyCalculationPhaseImpl(
    fkTaskQueue: ForeignKeyTaskQueue,
    taskHandlers: Seq[ForeignKeyTaskHandler],
    pkStoreWorkflow: PkStoreWorkflow,
    keyIngester: KeyIngester
) extends ForeignKeyCalculationPhase {

  private[this] val counter: AtomicLong = new AtomicLong(fkTaskQueue.size())

  override def runPhase(): Unit = {
    val executorService: ExecutorService =
      Executors.newFixedThreadPool(taskHandlers.size)

    val latch: CountDownLatch =
      new CountDownLatch(taskHandlers.size)

    taskHandlers.foreach { taskHandler =>
      executorService.submit { () =>
        try {
          calculateTillExhausted(taskHandler)
          latch.countDown()
          Unit
        } catch {
          case e: Throwable =>
            e.printStackTrace()
            System.exit(1)
        }
      }
    }

    latch.await()
    executorService.shutdownNow()
  }

  private def calculateTillExhausted(taskHandler: ForeignKeyTaskHandler): Unit = {
    while (counter.get() > 0) {
      var nextTask = fkTaskQueue.dequeue()
      while (nextTask.nonEmpty) {
        val newTasksAdded: Long = handle(taskHandler, nextTask.get)
        counter.addAndGet(newTasksAdded - 1)
        nextTask = fkTaskQueue.dequeue()
      }
      Thread.sleep(50)
    }
  }

  private def handle(taskHandler: ForeignKeyTaskHandler, task: ForeignKeyTask): Long = {
    val isDuplicate: Boolean =
      task match {
        case fetchParentTask: FetchParentTask if FkTaskPreCheck.shouldPrecheck(fetchParentTask) =>
          val tableToCheck: Table = fetchParentTask.fk.toTable
          val primaryKeyValueToCheck: PrimaryKeyValue =
            new PrimaryKeyValue(fetchParentTask.fkValueFromChild.individualColumnValues)
          pkStoreWorkflow.alreadySeen(tableToCheck, primaryKeyValueToCheck)
        case _ => false
      }

    if (isDuplicate) {
      0
    } else {
      val dbResult = taskHandler.handle(task)
      var newTaskCount = 0L
      newTaskCount = keyIngester.ingest(dbResult)
      newTaskCount
    }
  }
}
