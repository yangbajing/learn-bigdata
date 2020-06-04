package spendreport

import org.scalatest.funsuite.AnyFunSuite

class FraudDetectionJobTest extends AnyFunSuite {
  test("run") { FraudDetectionJob.main(Array()) }
}
