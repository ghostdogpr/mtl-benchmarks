package ck.benchmarks

import cats.data.Chain
import cats.kernel.Monoid
import cats.mtl._
import cats.{ Applicative, Functor, Monad }
import ck.benchmarks.Test._
import zio.{ Ref, Tag, ZIO }

object ZioInstances {

  type ZIOReaderWriterState[E, L, S, +A] = ZIO[E with Ref[S] with Ref[L], Throwable, A]

  implicit def zioApplicativeAsk[E: Tag, L, S](
    implicit ev: Applicative[ZIOReaderWriterState[E, L, S, *]],
    monoid: Monoid[L]
  ): Ask[ZIOReaderWriterState[E, L, S, *], E] =
    new Ask[ZIOReaderWriterState[E, L, S, *], E] {
      override val applicative: Applicative[ZIOReaderWriterState[E, L, S, *]] = ev
      override def ask[E2 >: E]: ZIOReaderWriterState[E, L, S, E2]            = ZIO.service[E]
    }

  implicit def zioFunctorTell[E, L, S](
    implicit ev: Functor[ZIOReaderWriterState[E, L, S, *]],
    monoid: Monoid[L],
    tag: Tag[Ref[L]]
  ): Tell[ZIOReaderWriterState[E, L, S, *], L] =
    new Tell[ZIOReaderWriterState[E, L, S, *], L] {
      override val functor: Functor[ZIOReaderWriterState[E, L, S, *]] = ev
      override def tell(l: L): ZIOReaderWriterState[E, L, S, Unit] =
        ZIO.serviceWithZIO[Ref[L]](_.update(log => monoid.combine(log, l)))
    }

  implicit def zioMonadState[E, L, S](
    implicit ev: Monad[ZIOReaderWriterState[E, L, S, *]],
    tag: Tag[Ref[S]]
  ): Stateful[ZIOReaderWriterState[E, L, S, *], S] =
    new Stateful[ZIOReaderWriterState[E, L, S, *], S] {
      override val monad: Monad[ZIOReaderWriterState[E, L, S, *]] = ev
      override def get: ZIOReaderWriterState[E, L, S, S]          = ZIO.serviceWithZIO[Ref[S]](_.get)
      override def set(s: S): ZIOReaderWriterState[E, L, S, Unit] = ZIO.serviceWithZIO[Ref[S]](_.set(s))
    }

  implicit def zioMonad[E, L, S]: Monad[ZIOReaderWriterState[E, L, S, *]] =
    new Monad[ZIOReaderWriterState[E, L, S, *]] {
      override def pure[A](x: A): ZIOReaderWriterState[E, L, S, A] = ZIO.succeed(x)
      override def flatMap[A, B](fa: ZIOReaderWriterState[E, L, S, A])(
        f: A => ZIOReaderWriterState[E, L, S, B]
      ): ZIOReaderWriterState[E, L, S, B] = fa.flatMap(f)
      override def tailRecM[A, B](
        a: A
      )(f: A => ZIOReaderWriterState[E, L, S, Either[A, B]]): ZIOReaderWriterState[E, L, S, B] = f(a).flatMap {
        case Left(a)  => tailRecM(a)(f)
        case Right(b) => ZIO.succeed(b)
      }
    }

  implicit val m1: Monad[P]              = zioMonad[Env, Chain[Event], State]
  implicit val m2: Ask[P, Env]           = zioApplicativeAsk[Env, Chain[Event], State]
  implicit val m3: Tell[P, Chain[Event]] = zioFunctorTell[Env, Chain[Event], State]
  implicit val m4: Stateful[P, State]    = zioMonadState[Env, Chain[Event], State]
}
