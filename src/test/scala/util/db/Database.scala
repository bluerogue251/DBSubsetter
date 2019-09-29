package util.db

trait Database {
  def name: String
  def port: Int
  def connectionString: String
}

class MySqlDatabase(val name: String, val port: Int) extends Database {
  override def connectionString: String = s"jdbc:mysql://localhost:$port/$name?user=root&useSSL=false&rewriteBatchedStatements=true"
}

class PostgreSQLDatabase(val host: String, val port: Int, val name: String) extends Database {
  override def connectionString: String = s"jdbc:postgresql://$host:$port/$name?user=postgres"
}

class SqlServerDatabase(val name: String, val port: Int) extends Database {
  override def connectionString: String = s"jdbc:sqlserver://localhost:$port;databaseName=$name;user=sa;password=MsSqlServerLocal1"
}