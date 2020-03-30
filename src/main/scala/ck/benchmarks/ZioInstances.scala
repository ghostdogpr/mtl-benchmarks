package ck.benchmarks

import cats.data.Chain
import cats.{ Applicative, Functor, Monad }
import cats.kernel.Monoid
import cats.mtl._
import ck.benchmarks.Test._
import izumi.reflect.Tags.Tag
import zio.{ Has, Ref, ZIO }

object ZioInstances {

  type ZIOReaderWriterState[E, +L, S, +A] = ZIO[Has[E] with Has[Ref[S]], Nothing, (L, A)]

  implicit def zioApplicativeAsk[E: Tag, L, S](
    implicit ev: Applicative[ZIOReaderWriterState[E, L, S, *]],
    monoid: Monoid[L]
  ): ApplicativeAsk[ZIOReaderWriterState[E, L, S, *], E] =
    new DefaultApplicativeAsk[ZIOReaderWriterState[E, L, S, *], E] {
      override val applicative: Applicative[ZIOReaderWriterState[E, L, S, *]] = ev
      override def ask: ZIOReaderWriterState[E, L, S, E]                      = ZIO.access[Has[E]](_.get).map(env => (monoid.empty, env))
    }

  implicit def zioFunctorTell[E, L, S](
    implicit ev: Functor[ZIOReaderWriterState[E, L, S, *]]
  ): FunctorTell[ZIOReaderWriterState[E, L, S, *], L] =
    new DefaultFunctorTell[ZIOReaderWriterState[E, L, S, *], L] {
      override val functor: Functor[ZIOReaderWriterState[E, L, S, *]] = ev
      override def tell(l: L): ZIOReaderWriterState[E, L, S, Unit]    = ZIO.succeed((l, ()))
    }

  implicit def zioMonadState[E: Tag, L, S: Tag](
    implicit ev: Monad[ZIOReaderWriterState[E, L, S, *]],
    monoid: Monoid[L]
  ): MonadState[ZIOReaderWriterState[E, L, S, *], S] =
    new DefaultMonadState[ZIOReaderWriterState[E, L, S, *], S] {
      override val monad: Monad[ZIOReaderWriterState[E, L, S, *]] = ev
      override def get: ZIOReaderWriterState[E, L, S, S] =
        ZIO.accessM[Has[Ref[S]]](_.get.get.map(state => (monoid.empty, state)))
      override def set(s: S): ZIOReaderWriterState[E, L, S, Unit] =
        ZIO.accessM[Has[Ref[S]]](_.get.set(s).as((monoid.empty, ())))
    }

  implicit def zioMonad[E: Tag, L, S: Tag](implicit monoid: Monoid[L]): Monad[ZIOReaderWriterState[E, L, S, *]] =
    new Monad[ZIOReaderWriterState[E, L, S, *]] {
      override def pure[A](x: A): ZIOReaderWriterState[E, L, S, A] = ZIO.succeed((monoid.empty, x))
      override def flatMap[A, B](
        fa: ZIOReaderWriterState[E, L, S, A]
      )(f: A => ZIOReaderWriterState[E, L, S, B]): ZIOReaderWriterState[E, L, S, B] =
        for {
          (log1, a) <- fa
          (log2, b) <- f(a)
        } yield (monoid.combine(log1, log2), b)
      override def tailRecM[A, B](
        a: A
      )(f: A => ZIOReaderWriterState[E, L, S, Either[A, B]]): ZIOReaderWriterState[E, L, S, B] = f(a).flatMap {
        case (l, Left(a))  => tailRecM(a)(f).map { case (l2, b) => (monoid.combine(l, l2), b) }
        case (l, Right(b)) => ZIO.succeed((l, b))
      }
    }

  implicit val m1: Monad[P]                     = zioMonad[Env, Chain[Event], State]
  implicit val m2: ApplicativeAsk[P, Env]       = zioApplicativeAsk[Env, Chain[Event], State]
  implicit val m3: FunctorTell[P, Chain[Event]] = zioFunctorTell[Env, Chain[Event], State]
  implicit val m4: MonadState[P, State]         = zioMonadState[Env, Chain[Event], State]

}
