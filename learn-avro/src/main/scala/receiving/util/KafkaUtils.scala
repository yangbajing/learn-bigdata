package receiving.util

import akka.http.scaladsl.model.Uri

import scala.collection.mutable

object KafkaUtils {
  def generateTopic(path: Uri.Path): String = {
    val ss = mutable.Buffer[String]()
    var node = path
    while (!node.isEmpty) {
      node.head match {
        case str: String => ss.append(str)
        case _           => // do nothing
      }
      node = node.tail
    }
    val topic = ss.mkString(".")
    topic
  }
}
