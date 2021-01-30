package forex.services.rates.interpreters

import cats.effect.Sync
import forex.config.OneFrameConfig
import forex.domain.{ Currency, Price, Rate, Timestamp }
import forex.services.rates.Algebra
import forex.services.rates.errors.Error
import io.circe.Decoder
import org.http4s.client.Client
import org.http4s.{ EntityDecoder, Header, Query, Request }
import io.circe.generic.semiauto.deriveDecoder
import org.http4s.circe._
import cats.syntax.show._
import cats.syntax.functor._
import cats.syntax.applicativeError._
import cats.syntax.either._
import forex.services.rates.errors.Error.OneFrameLookupFailed
import forex.http._

class OneFrameHttp[F[_]: Sync](config: OneFrameConfig, http: Client[F]) extends Algebra[F] {

  private case class OneFrameExchange(from: Currency, to: Currency, price: Price, time_stamp: Timestamp)

  private implicit val rateDecoder: Decoder[OneFrameExchange] =
    deriveDecoder[OneFrameExchange]
  private implicit val rateEDecoder: EntityDecoder[F, List[OneFrameExchange]] =
    jsonOf[F, List[OneFrameExchange]]

  override def get(pair: Rate.Pair): F[Error Either Rate] =
    getMany(Set(pair))
      .map { eith =>
        eith.flatMap(_.find(r => r.pair == pair).toRight(OneFrameLookupFailed("Can't find exchange rate right now")))
      }

  override def getMany(pairs: Set[Rate.Pair]): F[Error Either List[Rate]] = {
    val q = Query.fromVector(pairs.map(p => "pair" -> Some(show"${p.from}${p.to}")).toVector)
    val req = Request[F]()
      .withHeaders(Header("token", config.token))
      .withUri(config.uri.copy(query = q, path = "/rates"))
    http.expect[List[OneFrameExchange]](req).attempt.map {
      case Right(xs) => xs.map(r => Rate(Rate.Pair(r.from, r.to), price = r.price, timestamp = r.time_stamp)).asRight
      case Left(err) => OneFrameLookupFailed("Can't find exchange rate right now").asLeft
    }
  }

}
