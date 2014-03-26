package actors

import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging
import play.libs.Akka
import akka.actor.ActorRef
import scala.collection.mutable.Map
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.concurrent.Execution.Implicits._
import app.model.Event

case class Join(id: Long)
case class Leave(id: Long)
case class Joined(enumerator: Enumerator[Event])

class EventSource(name: String) extends Actor {
  val log = Logging(context.system, this)
  val connected = Map[Long, Channel[Event]]()
  /** Returns the current instance of the driver. */

  def receive = {
    case event: Event => {
      log.info("event: " + event.name)
      pushEventToConnectedEnumerators(event)
    }

    case Join(channelId: Long) => {
      log.error("Connecting")
      val e = Concurrent.unicast[Event] { c =>
        connected += (channelId -> c)
      }
      sender ! Joined(e)
    }

    case Leave(id: Long) => {
      connected -= id
    }

    case e => {
      log.error("Unknown message: " + e)
    }

  }

  private def pushEventToConnectedEnumerators(event: Event): Unit = {
    connected.values foreach (_ push event)
  }
}

object EventSource {
  def props(name: String): Props = Props(classOf[EventSource], name)
  val eventSource = Akka.system().actorOf(props("hello"))

}