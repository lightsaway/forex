package forex.services.rates.interpreters

import java.time.OffsetDateTime

import cats.effect.concurrent.Ref
import cats.effect.{ Clock, IO }
import forex.domain.{ Currency, Price, Rate, Timestamp }
import forex.services.rates.errors.Error.OneFrameLookupFailed
import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.collection.immutable.HashMap

class OneFrameCacheTest extends AnyWordSpec with Matchers with EitherValues {
  implicit val clock: Clock[IO] = Clock.create[IO]

  val rate = Rate(Rate.Pair(Currency.SGD, Currency.GBP), Price(0.6461108287915923), Timestamp.now)

  "get one" in {
    val test = for {
      cache <- Ref.of[IO, HashMap[Rate.Pair, Rate]](HashMap((rate.pair -> rate)))
      res <- new OneFrameCache[IO](cache).get(rate.pair)
      _ = res.right.value shouldBe rate
    } yield ()
    test.unsafeRunSync()
  }

  "get one not existing" in {
    val test = for {
      cache <- Ref.of[IO, HashMap[Rate.Pair, Rate]](HashMap((rate.pair -> rate)))
      res <- new OneFrameCache[IO](cache).get(rate.pair.copy(from = Currency.USD))
      _ = res.left.value shouldBe a[OneFrameLookupFailed]
    } yield ()
    test.unsafeRunSync()
  }

  "get one too old" in {
    val test = for {
      cache <- Ref.of[IO, HashMap[Rate.Pair, Rate]](
                HashMap(
                  (rate.pair -> rate.copy(timestamp = Timestamp(OffsetDateTime.parse("2021-01-30T10:25:36.505Z"))))
                )
              )
      res <- new OneFrameCache[IO](cache).get(rate.pair)
      _ = res.left.value shouldBe a[OneFrameLookupFailed]
    } yield ()
    test.unsafeRunSync()
  }

}
