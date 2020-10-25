package trw.dbsubsetter.datacopy

import java.util.concurrent.{CountDownLatch, ExecutorService, Executors}

private[datacopy] final class DataCopyPhaseImpl(queue: DataCopyQueue, copiers: Seq[DataCopier]) extends DataCopyPhase {

  override def runPhase(): Unit = {
    val executorService: ExecutorService =
      Executors.newFixedThreadPool(copiers.size)

    val latch: CountDownLatch =
      new CountDownLatch(copiers.size)

    copiers.foreach { copier =>
      executorService.submit { () =>
        try {
          copyTillExhausted(copier)
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

  private def copyTillExhausted(copier: DataCopier): Unit = {
    var nextTask = queue.dequeue()
    while (nextTask.nonEmpty) {
      copier.runTask(nextTask.get)
      nextTask = queue.dequeue()
    }
  }
}
