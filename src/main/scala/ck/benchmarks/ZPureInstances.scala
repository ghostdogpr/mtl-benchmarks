package ck.benchmarks

import cats.mtl._
import cats.{ Applicative, Functor, Monad }
import ck.benchmarks.Test._
import zio.prelude.fx.ZPure

object ZPureInstances {

  implicit def zPureApplicativeAsk[E, L, S](
    implicit ev: Applicative[ZPure[L, S, S, E, Nothing, *]]
  ): Ask[ZPure[L, S, S, E, Nothing, *], E] =
    new Ask[ZPure[L, S, S, E, Nothing, *], E] {
      override val applicative: Applicative[ZPure[L, S, S, E, Nothing, *]] = ev
      override def ask[E2 >: E]: ZPure[L, S, S, E, Nothing, E2]            = ZPure.environment
    }

  implicit def zPureFunctorTell[E, L, S](
    implicit ev: Functor[ZPure[L, S, S, E, Nothing, *]]
  ): Tell[ZPure[L, S, S, E, Nothing, *], L] =
    new Tell[ZPure[L, S, S, E, Nothing, *], L] {
      override val functor: Functor[ZPure[L, S, S, E, Nothing, *]] = ev
      override def tell(l: L): ZPure[L, S, S, E, Nothing, Unit]    = ZPure.log(l)
    }

  implicit def zPureMonadState[E, L, S](
    implicit ev: Monad[ZPure[L, S, S, E, Nothing, *]]
  ): Stateful[ZPure[L, S, S, E, Nothing, *], S] =
    new Stateful[ZPure[L, S, S, E, Nothing, *], S] {
      override val monad: Monad[ZPure[L, S, S, E, Nothing, *]] = ev
      override def get: ZPure[L, S, S, E, Nothing, S]          = ZPure.get
      override def set(s: S): ZPure[L, S, S, E, Nothing, Unit] = ZPure.set(s)
    }

  implicit def zPureMonad[E, L, S]: Monad[ZPure[L, S, S, E, Nothing, *]] =
    new Monad[ZPure[L, S, S, E, Nothing, *]] {
      override def pure[A](x: A): ZPure[L, S, S, E, Nothing, A] = ZPure.succeed(x)
      override def flatMap[A, B](fa: ZPure[L, S, S, E, Nothing, A])(
        f: A => ZPure[L, S, S, E, Nothing, B]
      ): ZPure[L, S, S, E, Nothing, B] = fa.flatMap(f)
      override def tailRecM[A, B](a: A)(
        f: A => ZPure[L, S, S, E, Nothing, Either[A, B]]
      ): ZPure[L, S, S, E, Nothing, B] =
        f(a).flatMap {
          case Left(a)  => tailRecM(a)(f)
          case Right(b) => ZPure.succeed(b)
        }
    }

  implicit val z1: Monad[P4]           = zPureMonad[Env, Event, State]
  implicit val z2: Ask[P4, Env]        = zPureApplicativeAsk[Env, Event, State]
  implicit val z3: Tell[P4, Event]     = zPureFunctorTell[Env, Event, State]
  implicit val z4: Stateful[P4, State] = zPureMonadState[Env, Event, State]

}
