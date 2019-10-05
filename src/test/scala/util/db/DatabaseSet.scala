package util.db

class DatabaseSet[T <: Database](
  val origin: T,
  val targetSingleThreaded: T,
  val targetAkkaStreams: T
)