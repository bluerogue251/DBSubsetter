package trw.dbsubsetter.akkastreams

import java.util.concurrent.{Executor, Executors}

import trw.dbsubsetter.datacopy.DataCopierFactory
import trw.dbsubsetter.datacopyqueue.DataCopyQueue
import trw.dbsubsetter.workflow.DataCopyTask

final class DataCopyTaskRunnerImpl(queue: DataCopyQueue, copierFactory: DataCopierFactory, parallelism: Int)
    extends DataCopyTaskRunner {

  private[this] val executor: Executor = Executors.newFixedThreadPool(parallelism)

  override def run(): Unit = {
    (0 until parallelism).foreach()
  }

  private def process(): Unit = {
    val workflow =
    var task = next()
    while (task.nonEmpty) {
     workflow.
    }
  }

  private def next(): Option[DataCopyTask] = {
    this.synchronized {
      queue.dequeue()
    }
  }
}
