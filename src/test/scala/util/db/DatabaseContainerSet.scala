package util.db

class DatabaseContainerSet[T <: Database](
  val origin: DatabaseContainer[T],
  val targetSingleThreaded: DatabaseContainer[T],
  val targetAkkaStreams: DatabaseContainer[T]
)