package trw.dbsubsetter.chronicle

import java.nio.file.Path

import net.openhft.chronicle.queue.RollCycles
import net.openhft.chronicle.queue.impl.single.{SingleChronicleQueue, SingleChronicleQueueBuilder}

object ChronicleQueueFactory {

  def createQueue(storageDirectory: Path): SingleChronicleQueue = {
    SingleChronicleQueueBuilder
      .binary(storageDirectory)
      .rollCycle(RollCycles.MINUTELY)
      .build()
  }
}
