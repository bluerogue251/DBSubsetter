package trw.dbsubsetter.akkastreams

import java.util.concurrent.{CountDownLatch, ExecutorService, Executors}

import trw.dbsubsetter.datacopy.DataCopier
import trw.dbsubsetter.datacopyqueue.DataCopyQueue
import trw.dbsubsetter.workflow.DataCopyTask

final class DataCopyPhaseImpl(queue: DataCopyQueue, copiers: Seq[DataCopier]) extends DataCopyPhase {

  private[this] val guard: Object = new Object()

  override def runPhase(): Unit = {
    val executorService: ExecutorService =
      Executors.newFixedThreadPool(copiers.size)

    val latch: CountDownLatch =
      new CountDownLatch(copiers.size)

    copiers.foreach { copier =>
      executorService.submit { () =>
        copyTillExhausted(copier)
        latch.countDown()
        Unit
      }
    }

    latch.await()
    executorService.shutdownNow()
  }

  private def copyTillExhausted(copier: DataCopier): Unit = {
    var nextTask = dequeueTask()
    while (nextTask.nonEmpty) {
      copier.runTask(nextTask.get)
      nextTask = dequeueTask()
    }
  }

  private def dequeueTask(): Option[DataCopyTask] = {
    guard.synchronized {
      queue.dequeue()
    }
  }
}
