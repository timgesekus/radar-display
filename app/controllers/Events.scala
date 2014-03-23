package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.JsValue
import play.api.libs.iteratee.Iteratee
import actors.EventSource.eventSource
import app.model.Event
import play.api.libs.concurrent.Execution.Implicits._
import actors.Leave
import actors.Join
import akka.pattern.{ ask, pipe }
import scala.concurrent.duration._
import akka.util.Timeout
import actors.Joined
import play.api.libs.EventSource
import play.libs.Akka
import actors.LabelModelEventGenerator
import actors.Start
import actors.Stopit
import play.api.libs.iteratee.Enumeratee
import play.api.libs.iteratee.Input
import play.api.libs.iteratee.Enumerator

object Events extends Controller {
  implicit val timeout = Timeout(5 seconds)
  def events = WebSocket.async[JsValue] { request =>
    play.Logger.error("Opening websocket")
    val generator = Akka.system().actorOf(LabelModelEventGenerator.props("generator", eventSource))
    val id = request.id
    // Log events to the console
    val in = Iteratee.foreach[JsValue] { event =>
      play.Logger.error("asdfsdf")
    }
    
    val done: Enumeratee[JsValue,JsValue] = Enumeratee.onIterateeDone({ () => 
      generator ! Stopit()
      eventSource ! Leave(id)
    })

    (eventSource ? Join(id)) map {
      case Joined(eumerator) =>
        generator ! Start()
        (in, eumerator &> Event.toJson &> done)
    }
  }

}