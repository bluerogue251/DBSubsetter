package trw.dbsubsetter.akkastreams

import java.util.concurrent.{CountDownLatch, ExecutorService, Executors}

import trw.dbsubsetter.datacopy.{DataCopier, DataCopierFactory}
import trw.dbsubsetter.datacopyqueue.DataCopyQueue
import trw.dbsubsetter.workflow.DataCopyTask

final class DataCopyTaskRunnerImpl(queue: DataCopyQueue, copierFactory: DataCopierFactory, parallelism: Int)
    extends DataCopyTaskRunner {

  override def run(): Unit = {
    val executorService: ExecutorService = Executors.newFixedThreadPool(parallelism)
    val latch: CountDownLatch = new CountDownLatch(parallelism)

    (1 to parallelism).foreach { _ =>
      val copier: DataCopier = copierFactory.build()
      var nextTask = dequeueTask()
      while (nextTask.nonEmpty) {
        copier.copy(nextTask.get)
        nextTask = dequeueTask()
      }
      latch.countDown()
    }

    latch.await()
    executorService.shutdownNow()
  }

  private def dequeueTask(): Option[DataCopyTask] = {
    synchronized {
      queue.dequeue()
    }
  }
}
