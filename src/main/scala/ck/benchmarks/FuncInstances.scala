package ck.benchmarks

import cats.data.Chain
import cats.effect.SyncIO
import cats.kernel.Monoid
import cats.mtl._
import cats.{ Applicative, Functor, Monad }
import ck.benchmarks.Test._

object FuncInstances {

  type Func[E, +L, S, +A] = (E, S) => SyncIO[(S, L, A)]

  implicit def testApplicativeAsk[E, L, S](
    implicit ev: Applicative[Func[E, L, S, *]],
    monoid: Monoid[L]
  ): ApplicativeAsk[Func[E, L, S, *], E] =
    new DefaultApplicativeAsk[Func[E, L, S, *], E] {
      override val applicative: Applicative[Func[E, L, S, *]] = ev
      override def ask: Func[E, L, S, E] = {
        case (e, s) => SyncIO.pure((s, monoid.empty, e))
      }
    }

  implicit def testFunctorTell[E, L, S](implicit ev: Functor[Func[E, L, S, *]]): FunctorTell[Func[E, L, S, *], L] =
    new DefaultFunctorTell[Func[E, L, S, *], L] {
      override val functor: Functor[Func[E, L, S, *]] = ev
      override def tell(l: L): Func[E, L, S, Unit] = {
        case (_, s) => SyncIO.pure((s, l, ()))
      }
    }

  implicit def testMonadState[E, L, S](
    implicit ev: Monad[Func[E, L, S, *]],
    monoid: Monoid[L]
  ): MonadState[Func[E, L, S, *], S] =
    new DefaultMonadState[Func[E, L, S, *], S] {
      override val monad: Monad[Func[E, L, S, *]] = ev
      override def get: Func[E, L, S, S] = {
        case (_, s) => SyncIO.pure((s, monoid.empty, s))
      }
      override def set(s: S): Func[E, L, S, Unit] = {
        case (_, _) => SyncIO.pure((s, monoid.empty, ()))
      }
    }

  implicit def testMonad[E, L, S](implicit monoid: Monoid[L]): Monad[Func[E, L, S, *]] =
    new Monad[Func[E, L, S, *]] {
      override def pure[A](x: A): Func[E, L, S, A] = {
        case (_, s) => SyncIO.pure((s, monoid.empty, x))
      }
      override def flatMap[A, B](fa: Func[E, L, S, A])(f: A => Func[E, L, S, B]): Func[E, L, S, B] = {
        case (e, s) =>
          fa(e, s).flatMap {
            case (s, l1, a) =>
              f(a)(e, s).flatMap {
                case (s, l2, b) => SyncIO.pure((s, monoid.combine(l1, l2), b))
              }
          }
      }
      override def tailRecM[A, B](a: A)(f: A => Func[E, L, S, Either[A, B]]): Func[E, L, S, B] = {
        case (e, s) =>
          f(a)(e, s).flatMap {
            case (s, l1, Left(a)) =>
              tailRecM(a)(f)(e, s).flatMap {
                case (s, l2, b) => SyncIO.pure((s, monoid.combine(l1, l2), b))
              }
            case (s, l, Right(b)) => SyncIO.pure((s, l, b))
          }
      }
    }

  implicit val t1: Monad[P3]                     = testMonad[Env, Chain[Event], State]
  implicit val t2: ApplicativeAsk[P3, Env]       = testApplicativeAsk[Env, Chain[Event], State]
  implicit val t3: FunctorTell[P3, Chain[Event]] = testFunctorTell[Env, Chain[Event], State]
  implicit val t4: MonadState[P3, State]         = testMonadState[Env, Chain[Event], State]
}
