package forex

import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, Timer}
import forex.config.ApplicationConfig
import forex.domain.{Currency, Rate}
import forex.http.rates.RatesHttpRoutes
import forex.services._
import forex.programs._
import org.http4s._
import org.http4s.client.Client
import org.http4s.implicits._
import org.http4s.server.middleware.{AutoSlash, Timeout}
import cats.implicits._

import scala.collection.immutable.HashMap

class Module[F[_]: Concurrent: Timer](config: ApplicationConfig, client: Client[F], cache : Ref[F, HashMap[Rate.Pair, Rate]]) {

  private val ratesService: RatesService[F] = RatesServices.cached[F](cache)

  private val ratesServiceHttp: RatesService[F] = RatesServices.http[F](config.oneFrame, client)

  private val ratesProgram: RatesProgram[F] = RatesProgram[F](ratesService)

  private val ratesHttpRoutes: HttpRoutes[F] = new RatesHttpRoutes[F](ratesProgram).routes

  type PartialMiddleware = HttpRoutes[F] => HttpRoutes[F]
  type TotalMiddleware   = HttpApp[F] => HttpApp[F]

  private val routesMiddleware: PartialMiddleware = {
    { http: HttpRoutes[F] =>
      AutoSlash(http)
    }
  }

  private val appMiddleware: TotalMiddleware = { http: HttpApp[F] =>
    Timeout(config.http.timeout)(http)
  }

  private val http: HttpRoutes[F] = ratesHttpRoutes

  val httpApp: HttpApp[F] = appMiddleware(routesMiddleware(http).orNotFound)

  val cacheRefresher = ratesServiceHttp.getMany(Currency.combinations).flatMap{xs => xs.map(x => cache.update{ old => old + (x.pair-> x) } ).sequence}
}
