package forex.config

import cats.effect.Sync
import fs2.Stream

import pureconfig.ConfigSource
import pureconfig.generic.auto._
import pureconfig.module.http4s._

object Config {

  /**
   * @param path the property path inside the default configuration
   */
  def stream[F[_]: Sync](path: String): Stream[F, ApplicationConfig] = {
    Stream.eval(Sync[F].delay(
      ConfigSource.default.at(path).loadOrThrow[ApplicationConfig]))
  }

}
