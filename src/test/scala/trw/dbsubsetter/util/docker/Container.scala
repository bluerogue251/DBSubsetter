package trw.dbsubsetter.util.docker

trait Container[T] {
  def name: String
  def process: T
}
