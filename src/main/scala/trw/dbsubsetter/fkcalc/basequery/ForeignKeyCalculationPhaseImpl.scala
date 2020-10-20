package trw.dbsubsetter.fkcalc.basequery

import java.util.concurrent.{CountDownLatch, ExecutorService, Executors}

final class ForeignKeyCalculationPhaseImpl() extends ForeignKeyCalculationPhase {

  private[this] val guard: Object = new Object()

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
