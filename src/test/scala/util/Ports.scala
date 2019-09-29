package util

object Ports {
  val sharedPostgresPort: Int = 5432
  val sharedSqlServerPort: Int = 5496
  val sharedMySqlOriginPort: Int = 5497
  val sharedMySqlTargetSingleThreadedPort: Int = 5498
  val sharedMySqlTargetAkkaStreamsPort: Int = 5499
  val postgresPhysicsDbOrigin: Int = 5501
  val postgresSchoolDbOrigin: Int = 5551
}
