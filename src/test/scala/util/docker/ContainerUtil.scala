package util.docker

import scala.sys.process._

object ContainerUtil {
  def start(name: String): Unit = {
    s"docker start $name".!
  }

  def exists(name: String): Boolean = {
    val commmandOutput = s"docker ps -a -q -f name=$name".!!
    commmandOutput.nonEmpty
  }
}
