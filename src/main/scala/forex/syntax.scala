package forex

private[forex] final class EffectToStream[F[_], A](private val fa: F[A]) extends AnyVal {
  @inline
  def stream: fs2.Stream[F, A] = fs2.Stream.eval(fa)
}

trait StreamSyntaxOps {

  implicit final def streamSyntaxOps[F[_], A](fa: F[A]): EffectToStream[F, A] =
    new EffectToStream[F, A](fa)

}

case object syntax extends StreamSyntaxOps
