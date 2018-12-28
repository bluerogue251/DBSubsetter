package util.db

trait Database {
  def name: String
  def connectionString: String
}
