package util.db

import util.docker.Container

class DatabaseContainer(container: Container[Database]) {
  def name: String = container.name
  def db: Database = container.process
}