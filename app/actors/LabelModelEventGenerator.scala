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
import scala.util.Random

case class Tick()
case class Start()
case class Stopit()

class LabelModelEventGenerator(name: String, eventSource: ActorRef) extends Actor {
  val log = Logging(context.system, this)
  val callsignfield = Field("DLH234", "normal")
  val wtcField = Field("M", "normal")
  var numAc = 1000
  var counter = 0
  var labelModels = Array[LabelModel]()
  var rand = new Random()
  var labelModel = LabelModel(1, Map(("CALLSIGN" -> callsignfield), "WTC" -> wtcField), "concerned", 20, 20)
  def receive() = {
    case Start() => {
      0 until numAc foreach ({ i =>
        val callsignfield = Field("DLH" + i.toString, "normal")
        val x = rand.nextInt(800) + 20
        val y = rand.nextInt(800) + 20
        val lm = LabelModel(i, Map(("CALLSIGN" -> callsignfield), "WTC" -> wtcField), "concerned", x, y)
        labelModels = labelModels :+ lm
      })
      sendEvent("Add")
      val cancellable =
        Akka.system().scheduler.schedule(0 milliseconds,
          (5000) / numAc milliseconds,
          self,
          Tick())
      context.become(sending(cancellable))
    }
  }

  def sending(cancellable: Cancellable): Receive = {
    case Tick() => {
      var xInc = rand.nextInt(3) - 1
      var yInc = rand.nextInt(3) - 1

      labelModels(counter) = labelModels(counter).copy(x = labelModels(counter).x + xInc)
      labelModels(counter) = labelModels(counter).copy(y = labelModels(counter).y + yInc)

      eventSource ! Event("Update", "LabelModel", Json.toJson(labelModels(counter)))
      counter += 1
      if (counter == numAc) counter = 0
    }
    case Stopit() => {
      play.Logger.error("Canceling")
      cancellable.cancel()
      Akka.system().stop(self)
    }
  }

  def sendEvent(eventName: String) {
    labelModels foreach ({ labelModel =>
      eventSource ! Event(eventName, "LabelModel", Json.toJson(labelModel))
    })
  }

}

object LabelModelEventGenerator {
  def props(name: String, eventSource: ActorRef): Props = Props(classOf[LabelModelEventGenerator], name, eventSource)
}