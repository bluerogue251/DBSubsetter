package util.docker


trait Container[T] {
  def name: String
  def process: T
}
