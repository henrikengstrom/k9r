package global

import play.api.libs.concurrent.Akka

import scala.concurrent.ExecutionContext

object Dispatchers {
  import play.api.Play.current
  implicit val ioDispatcher: ExecutionContext = Akka.system.dispatchers.lookup("io-dispatcher")
}
