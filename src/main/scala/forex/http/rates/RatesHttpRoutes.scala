package forex.http
package rates

import cats.data.Validated.Valid
import cats.effect.Sync
import cats.implicits._
import forex.domain.Currency
import forex.programs.RatesProgram
import forex.programs.rates.Protocol.GetRatesRequest
import io.circe.Json
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

class RatesHttpRoutes[F[_]: Sync](rates: RatesProgram[F]) extends Http4sDsl[F] {

  import Converters._
  import Protocol._
  import QueryParams._

  private[http] val prefixPath = "/rates"
  private val supportedCurrenciesMsg = Json.obj(
    "error" -> Json.fromString("Unsupported currency pair"),
    "supported" -> Json.fromValues(Currency.combinations.map(p => Json.fromString(show"${p.from} -> ${p.to}")))
  )

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? FromQueryParam(from) +& ToQueryParam(to) =>
      (from, to).mapN(GetRatesRequest) match {
        case Valid(pair) if pair.from != pair.to =>
          rates.get(pair).flatMap(Sync[F].fromEither).flatMap { rate =>
            Ok(rate.asGetApiResponse)
          }
        case _ => BadRequest(supportedCurrenciesMsg)
      }
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
