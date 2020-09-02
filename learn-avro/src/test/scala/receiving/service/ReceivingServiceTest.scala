package receiving.service

import akka.http.scaladsl.model.Uri
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import receiving.util.KafkaUtils

class ReceivingServiceTest extends AnyWordSpec with Matchers {
  "Request" should {
    "topic" in {
      val path = Uri.Path("/com.xxxx.bbb/CarCredit")
      KafkaUtils.generateTopic(path) shouldBe "com.xxxx.bbb.CarCredit"
    }
  }
}
