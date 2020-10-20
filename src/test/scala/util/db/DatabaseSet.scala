package util.db

class DatabaseSet[T <: Database](
    val origin: T,
    val target: T
)
