package learnspark

import org.scalatest.{ Matchers, WordSpec, EitherValues, OptionValues }
import org.scalatest.concurrent.Futures

abstract class UnitWordSpec extends WordSpec with Futures with Matchers with EitherValues with OptionValues {}
