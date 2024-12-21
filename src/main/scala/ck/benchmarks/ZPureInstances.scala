package ck.benchmarks

import cats.mtl._
import cats.{Applicative, Functor, Monad}
import ck.benchmarks.Test._
import zio.prelude.fx.ZPure
import zio.Tag

object ZPureInstances {

  implicit def zPureApplicativeAsk[L, S, R: Tag, E](implicit
      ev: Applicative[ZPure[L, S, S, R, E, *]]
  ): Ask[ZPure[L, S, S, R, E, *], R] =
    new Ask[ZPure[L, S, S, R, E, *], R] {
      override val applicative: Applicative[ZPure[L, S, S, R, E, *]] = ev
      override def ask[R2 >: R]: ZPure[L, S, S, R, E, R2]            = ZPure.service
    }

  implicit def zPureFunctorTell[L, S, R, E](implicit
      ev: Functor[ZPure[L, S, S, R, E, *]]
  ): Tell[ZPure[L, S, S, R, E, *], L] =
    new Tell[ZPure[L, S, S, R, E, *], L] {
      override val functor: Functor[ZPure[L, S, S, R, E, *]] = ev
      override def tell(l: L): ZPure[L, S, S, R, E, Unit]    = ZPure.log(l)
    }

  implicit def zPureMonadState[L, S, R, E](implicit
      ev: Monad[ZPure[L, S, S, R, E, *]]
  ): Stateful[ZPure[L, S, S, R, E, *], S] =
    new Stateful[ZPure[L, S, S, R, E, *], S] {
      override val monad: Monad[ZPure[L, S, S, R, E, *]] = ev
      override def get: ZPure[L, S, S, R, E, S]          = ZPure.get
      override def set(s: S): ZPure[L, S, S, R, E, Unit] = ZPure.set(s)
    }

  implicit def zPureMonad[L, S, R, E]: Monad[ZPure[L, S, S, R, E, *]] =
    new Monad[ZPure[L, S, S, R, E, *]] {
      override def pure[A](x: A): ZPure[L, S, S, R, E, A] = ZPure.succeed(x)
      override def flatMap[A, B](fa: ZPure[L, S, S, R, E, A])(
          f: A => ZPure[L, S, S, R, E, B]
      ): ZPure[L, S, S, R, E, B] = fa.flatMap(f)
      override def tailRecM[A, B](a: A)(
          f: A => ZPure[L, S, S, R, E, Either[A, B]]
      ): ZPure[L, S, S, R, E, B] =
        f(a).flatMap {
          case Left(a)  => tailRecM(a)(f)
          case Right(b) => ZPure.succeed(b)
        }
    }

  implicit val z1: Monad[P4]            = zPureMonad[Event, State, Environment, Throwable]
  implicit val z2: Ask[P4, Environment] = zPureApplicativeAsk[Event, State, Environment, Throwable]
  implicit val z3: Tell[P4, Event]      = zPureFunctorTell[Event, State, Environment, Throwable]
  implicit val z4: Stateful[P4, State]  = zPureMonadState[Event, State, Environment, Throwable]

}
