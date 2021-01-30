package forex.domain

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CurrencyTest extends AnyWordSpec with Matchers {

  "combinations should be proper" in {
    val count = Currency.values.size
    Currency.combinations.size shouldBe Math.pow(count, 2) - count
  }

}
