package app.model

import play.api.libs.json.Json


case class Field(value: String, style: String)
case class LabelModel(id: Int, values: Map[String, Field], layout: String, x: Int, y: Int)

object LabelModel {
  implicit val fielFormat = Json.format[Field]
  implicit val labelModelFormat = Json.format[LabelModel]

}