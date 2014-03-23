package actors

import akka.event.Logging
import play.libs.Akka
import akka.actor.Actor
import akka.actor.ActorRef
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import app.model.LabelModel
import app.model.Field
import app.model.Event
import play.api.libs.json.Json
import akka.actor.Props
import akka.actor.Cancellable

case class Tick()
case class Start()
case class Stopit()

class LabelModelEventGenerator(name: String, eventSource: ActorRef) extends Actor {
  val log = Logging(context.system, this)
  val callsignfield = Field("DLH234", "normal")
  val wtcField = Field("M", "normal")

  var labelModel = LabelModel(1, Map(("CALLSIGN" -> callsignfield), "WTC" -> wtcField), "concerned", 20, 20)
  def receive() = {
    case Start() => {
      sendEvent("Add")
      val cancellable =
        Akka.system().scheduler.schedule(0 milliseconds,
          50 milliseconds,
          self,
          Tick())
      context.become(sending(cancellable))      
    }
  }

  def sending(cancellable: Cancellable): Receive = {
    case Tick() => {
      labelModel = labelModel.copy(x = labelModel.x + 1)
      sendEvent("Update")
    }
    case Stopit() => {
      play.Logger.error("Canceling")
      cancellable.cancel()
      Akka.system().stop(self)
    }
  }

  def sendEvent(eventName: String) {
    eventSource ! Event(eventName, "LabelModel", Json.toJson(labelModel))
  }

}

object LabelModelEventGenerator {
  def props(name: String, eventSource: ActorRef): Props = Props(classOf[LabelModelEventGenerator], name, eventSource)
}