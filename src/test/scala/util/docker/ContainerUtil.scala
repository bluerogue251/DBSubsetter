package util.docker

import scala.sys.process._

object ContainerUtil {
  def rm(containerName: String): Unit = {
    s"docker rm --force --volumes $containerName".!
  }

  def start(containerName: String): Unit = {
    s"docker start $containerName".!
  }
}
