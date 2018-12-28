package util.docker

import java.net.URI


trait Container[T] {
  def name: String
  def uri: URI
  def process: T
}
