package forex

import cats.effect._
import cats.effect.concurrent.Ref
import cats.syntax.functor._
import forex.config._
import forex.domain.Rate
import fs2.Stream
import org.http4s.server.blaze.BlazeServerBuilder
import forex.syntax._
import org.http4s.client.blaze.BlazeClientBuilder
import scala.concurrent.ExecutionContext.global
import scala.collection.immutable.HashMap
import scala.concurrent.duration._

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    new Application[IO].stream.compile.drain.as(ExitCode.Success)

}

class Application[F[_]: ConcurrentEffect: Timer] {

  def stream: Stream[F, Unit] =
    for {
      config <- Config.stream("app")
      client <- BlazeClientBuilder[F](global).stream
      cache  <- Ref.of(HashMap.empty[Rate.Pair, Rate]).stream
      module = new Module[F](config, client, cache)
      refresher = module.cacheRefresher.stream
      _ <- refresher
      refreshing = refresher.delayBy(120.seconds).repeat
      server = BlazeServerBuilder[F]
            .bindHttp(config.http.port, config.http.host)
            .withHttpApp(module.httpApp)
            .serve
     _ <- fs2.Stream(server, refreshing).parJoin(2).void
    } yield ()

}
