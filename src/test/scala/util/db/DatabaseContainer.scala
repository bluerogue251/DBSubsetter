package util.db

import util.docker.Container

import scala.sys.process._

class DatabaseContainer[T <: Database](container: Container[T]) {
  def name: String = container.name
  def db: T = container.process
}

object DatabaseContainer {
  def createMySqlContainer(name: String, port: Int): Unit = {
    s"docker create --name $name -p $port:3306 --env MYSQL_ALLOW_EMPTY_PASSWORD=true mysql:8.0.3".!!
  }

  def createPostgreSQLContainer(name: String, port: Int): Unit = {
    s"docker create --name $name -p $port:5432 postgres:9.6.3".!!
  }

}