package forex.services.rates.interpreters

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

import cats.FlatMap
import cats.effect.Clock
import cats.effect.concurrent.Ref
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import forex.domain.Rate
import forex.services.rates.Algebra
import forex.services.rates.errors.Error.OneFrameLookupFailed
import forex.services.rates.errors._

import scala.collection.immutable.HashMap

class OneFrameCache[F[_]: FlatMap: Clock](db: Ref[F, HashMap[Rate.Pair, Rate]]) extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] = Clock[F].realTime(TimeUnit.MILLISECONDS).flatMap { now =>
    db.get
      .map { cache =>
        cache.get(pair)
      }
      .map {
        case Some(x) if x.timestamp.value.toInstant.isAfter(Instant.ofEpochMilli(now).minus(5, ChronoUnit.MINUTES)) =>
          x.asRight
        case _ => OneFrameLookupFailed("Can't find exchange rate right now").asLeft
      }
  }
  override def getMany(pairs: Set[Rate.Pair]): F[Error Either List[Rate]] = db.get.map { _.values.toList.asRight }
}
