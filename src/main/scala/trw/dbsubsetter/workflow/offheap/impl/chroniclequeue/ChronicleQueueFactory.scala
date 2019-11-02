package trw.dbsubsetter.workflow.offheap.impl.chroniclequeue

import java.nio.file.Files

import net.openhft.chronicle.queue.RollCycles
import net.openhft.chronicle.queue.impl.single.{SingleChronicleQueue, SingleChronicleQueueBuilder}
import trw.dbsubsetter.config.Config

object ChronicleQueueFactory {

  def createQueue(config: Config): SingleChronicleQueue = {
    val storageDir =
      config.taskQueueDirOpt match {
        case Some(dir) => dir.toPath
        case None => Files.createTempDirectory("DBSubsetter-")
      }

    SingleChronicleQueueBuilder
      .binary(storageDir)
      .rollCycle(RollCycles.MINUTELY)
      .build()
  }
}
