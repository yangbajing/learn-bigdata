package learnspark

import org.scalatest.concurrent.Futures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{ EitherValues, OptionValues }

abstract class UnitWordSpec extends AnyWordSpec with Futures with Matchers with EitherValues with OptionValues {}
