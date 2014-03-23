package app.model

import play.api.libs.json.Json
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.iteratee.Enumeratee
import play.api.libs.json.JsValue
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.Input
import play.api.libs.iteratee.Done

case class Event(name: String, contentType: String, content: JsValue)

object Event {
  implicit val eventFormat = Json.format[Event]
  val toJson = Enumeratee.map[Event]({ event => 
    Json.toJson(event)
  })
  val fromJson = Enumeratee.map[JsValue]({ jsvalue => jsvalue.as[Event] })
}