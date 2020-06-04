package getting.started

import org.scalatest.funsuite.AnyFunSuite

class SocketWindowWordCountTest extends AnyFunSuite {
  test("run") { SocketWindowWordCount.main(Array("--port", "9999")) }
}
