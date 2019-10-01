package util.db

trait DatabaseContainer[T <: Database] {
  def name: String
  def db: T
}

class MySqlContainer(val name: String, val db: MySqlDatabase) extends DatabaseContainer[MySqlDatabase]
class PostgreSQLContainer(val name: String, val db: PostgreSQLDatabase) extends DatabaseContainer[PostgreSQLDatabase]
class SqlServerContainer(val name: String, val db: SqlServerDatabase) extends DatabaseContainer[SqlServerDatabase]