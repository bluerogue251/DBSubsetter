package trw.dbsubsetter.akkastreams

import java.util.concurrent.{CountDownLatch, ExecutorService, Executors}

import trw.dbsubsetter.datacopy.DataCopier
import trw.dbsubsetter.datacopyqueue.DataCopyQueue
import trw.dbsubsetter.workflow.DataCopyTask

final class DataCopyTaskRunnerImpl(queue: DataCopyQueue, copiers: Seq[DataCopier]) extends DataCopyTaskRunner {

  override def run(): Unit = {
    val executorService: ExecutorService = Executors.newFixedThreadPool(copiers.size)
    val latch: CountDownLatch = new CountDownLatch(copiers.size)

    copiers.foreach { copier =>
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
    queue.synchronized {
      queue.dequeue()
    }
  }
}
