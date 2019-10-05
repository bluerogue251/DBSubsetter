package util

object Ports {
  val sharedPostgresPort: Int = 5432
  val sharedSqlServerPort: Int = 1433
  val sharedMySqlOriginPort: Int = 3306
  val sharedMySqlTargetSingleThreadedPort: Int = 3307
  val sharedMySqlTargetAkkaStreamsPort: Int = 3308
  val postgresPhysicsDbOrigin: Int = 5501
}
