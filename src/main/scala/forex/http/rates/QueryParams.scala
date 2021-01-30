package forex.http.rates

import forex.domain.Currency
import org.http4s.{ ParseFailure, QueryParamDecoder, QueryParameterValue }
import org.http4s.dsl.impl.QueryParamDecoderMatcher
import cats.implicits._

object QueryParams {

  private[http] implicit val currencyQueryParam: QueryParamDecoder[Currency] =
    (value: QueryParameterValue) =>
      QueryParamDecoder[String]
        .decode(value)
        .andThen(s => Currency.fromString(s).toValidNel(ParseFailure("invalid currency provided", "")))

  object FromQueryParam extends QueryParamDecoderMatcher[Currency]("from")
  object ToQueryParam extends QueryParamDecoderMatcher[Currency]("to")

}
