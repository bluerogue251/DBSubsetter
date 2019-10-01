package util.db

trait Database {
  def name: String
  def port: Int
  def connectionString: String
}

class MySqlDatabase(val host: String, val port: Int, val name: String) extends Database {
  override def connectionString: String = s"jdbc:mysql://$host:$port/$name?user=root&useSSL=false&rewriteBatchedStatements=true"
}

class PostgreSQLDatabase(val host: String, val port: Int, val name: String) extends Database {
  override def connectionString: String = s"jdbc:postgresql://$host:$port/$name?user=postgres"
}

class SqlServerDatabase(val host: String, val name: String, val port: Int) extends Database {
  override def connectionString: String = s"jdbc:sqlserver://$host:$port;databaseName=$name;user=sa;password=MsSqlServerLocal1"
}