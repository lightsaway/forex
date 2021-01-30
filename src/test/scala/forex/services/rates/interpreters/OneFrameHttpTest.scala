package forex.services.rates.interpreters

import java.time.OffsetDateTime

import cats.effect.IO
import forex.config.OneFrameConfig
import forex.domain.{ Currency, Price, Rate, Timestamp }
import io.circe.Json
import org.http4s.{ HttpRoutes, Request, Response, Uri }
import org.http4s.client.Client
import org.http4s.implicits._
import org.http4s.circe._
import io.circe.literal._
import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class OneFrameHttpTest extends AnyWordSpec with Matchers with EitherValues {

  private def client[T](f: Request[IO] => T, responseBody: Json) =
    Client.fromHttpApp(
      HttpRoutes
        .of[IO] {
          case req =>
            f(req)
            IO.pure(Response[IO]().withEntity(responseBody))
        }
        .orNotFound
    )
  val conf = OneFrameConfig(Uri.unsafeFromString("http://one-frame.io"), "token")
  val rate = Rate(
    Rate.Pair(Currency.SGD, Currency.GBP),
    Price(0.6461108287915923),
    Timestamp(OffsetDateTime.parse("2021-01-30T10:25:36.505Z"))
  )

  "get one" in {
    val expectedUri = Uri.unsafeFromString("http://one-frame.io/rates?pair=SGDGBP")
    val body =
      json"""
          [
            {
              "from": "SGD",
              "to": "GBP",
              "bid": 0.43871988670919926,
              "ask": 0.8535017708739854,
              "price": 0.6461108287915923,
              "time_stamp": "2021-01-30T10:25:36.505Z"
            }
          ]
          """
    val mockedClient = client(req => req.uri shouldBe expectedUri, body)
    val res          = new OneFrameHttp[IO](conf, mockedClient).get(rate.pair).unsafeRunSync
    res.right.value shouldBe rate
  }

  "get many" in {
    val expectedUri = Uri.unsafeFromString("http://one-frame.io/rates?pair=SGDGBP&pair=GBPSGD")
    val body =
      json"""
          [
            {
              "from": "SGD",
              "to": "GBP",
              "bid": 0.43871988670919926,
              "ask": 0.8535017708739854,
              "price": 0.6461108287915923,
              "time_stamp": "2021-01-30T10:25:36.505Z"
            },
            {
              "from": "GBP",
              "to": "SGD",
              "bid": 0.43871988670919926,
              "ask": 0.8535017708739854,
              "price": 0.6461108287915923,
              "time_stamp": "2021-01-30T10:25:36.505Z"
            }
          ]
          """
    val mockedClient     = client(req => req.uri shouldBe expectedUri, body)
    val pairs: Set[Rate] = Set(rate, rate.copy(pair = Rate.Pair(from = rate.pair.to, to = rate.pair.from)))
    val res              = new OneFrameHttp[IO](conf, mockedClient).getMany(pairs.map(_.pair)).unsafeRunSync
    res.right.value shouldBe pairs.toList
  }
}
