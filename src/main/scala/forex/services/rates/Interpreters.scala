package forex.services.rates

import cats.FlatMap
import cats.effect.concurrent.Ref
import cats.effect.{Clock, Sync}
import forex.config.OneFrameConfig
import forex.domain.Rate
import forex.services.rates.interpreters._
import org.http4s.client.Client

import scala.collection.immutable.HashMap

object Interpreters {
  def cached[F[_]: FlatMap: Clock](cache: Ref[F, HashMap[Rate.Pair, Rate]]): Algebra[F] = new OneFrameCache[F](cache)
  def http[F[_]: Sync](config: OneFrameConfig,http: Client[F]): Algebra[F] = new OneFrameHttp[F](config, http)
}
