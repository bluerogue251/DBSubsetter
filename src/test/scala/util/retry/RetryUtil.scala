package util.retry

import scala.annotation.tailrec
import scala.sys.process._

object RetryUtil {
  private val limitMillis: Int = 30000 // 30-second limit before we give up

  def withRetry(systemCommand: String): Unit = {
    val retryable: () => Result = () => {
      val exitCode: Int = systemCommand.!
      if (exitCode == 0) Success else Failure
    }
    withRetry(retryable, 0)
  }

  @tailrec
  private def withRetry(retryable: () => Result, alreadyWaitedMillis: Int): Unit = {
    retryable.apply() match {
      case Success => // Done
      case Failure =>
        if (alreadyWaitedMillis > limitMillis) throw new RuntimeException("Retry time threshold exceeded")
        Thread.sleep(500)
        withRetry(retryable, alreadyWaitedMillis + 500)
    }
  }
}

sealed trait Result
case object Success extends Result
case object Failure extends Result