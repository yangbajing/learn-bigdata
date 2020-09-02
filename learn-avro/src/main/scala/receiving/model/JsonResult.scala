package receiving.model

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.{ JsonNodeFactory, ObjectNode }
import de.heikoseeberger.akkahttpjackson.JacksonSupport

case class JsonResult(status: Int, message: String, data: JsonNode)

object JsonResult {
  def ok(): JsonResult = new JsonResult(200, "", new ObjectNode(JsonNodeFactory.instance))

  def ok(data: Object): JsonResult = ok(data, "")

  def ok(data: Object, message: String): JsonResult = {
    val json = JacksonSupport.defaultObjectMapper.valueToTree(data)
    new JsonResult(200, message, json)
  }

  def error(message: String): JsonResult = JsonResult(500, message, new ObjectNode(JsonNodeFactory.instance))

  def notFound(message: String): JsonResult = JsonResult(404, message, new ObjectNode(JsonNodeFactory.instance))
}
