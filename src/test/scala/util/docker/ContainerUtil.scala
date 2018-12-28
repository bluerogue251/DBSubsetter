package util.docker

import scala.sys.process._

object ContainerUtil {
  def rm(name: String): Unit = {
    s"docker rm --force --volumes $name".!
  }

  def start(name: String): Unit = {
    s"docker start $name".!
  }
}
