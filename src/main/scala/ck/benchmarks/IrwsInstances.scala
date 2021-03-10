package ck.benchmarks

import cats.{ Applicative, Functor, Monad }
import cats.data.IndexedReaderWriterStateT
import cats.kernel.Monoid
import cats.mtl._

object IrwsInstances {

  implicit def irwsApplicativeAsk[F[_]: Applicative, E, L, S](
    implicit ev: Applicative[IndexedReaderWriterStateT[F, E, L, S, S, *]],
    monoid: Monoid[L]
  ): Ask[IndexedReaderWriterStateT[F, E, L, S, S, *], E] =
    new Ask[IndexedReaderWriterStateT[F, E, L, S, S, *], E] {
      override val applicative: Applicative[IndexedReaderWriterStateT[F, E, L, S, S, *]] = ev
      override def ask[E2 >: E]: IndexedReaderWriterStateT[F, E, L, S, S, E2] =
        IndexedReaderWriterStateT[F, E, L, S, S, E2]((e, s) => Applicative[F].pure((monoid.empty, s, e)))
    }

  implicit def irwsFunctorTell[F[_]: Applicative, E, L, S](
    implicit ev: Functor[IndexedReaderWriterStateT[F, E, L, S, S, *]]
  ): Tell[IndexedReaderWriterStateT[F, E, L, S, S, *], L] =
    new Tell[IndexedReaderWriterStateT[F, E, L, S, S, *], L] {
      override val functor: Functor[IndexedReaderWriterStateT[F, E, L, S, S, *]] = ev
      override def tell(l: L): IndexedReaderWriterStateT[F, E, L, S, S, Unit]    = IndexedReaderWriterStateT.tell(l)
    }

  implicit def irwsMonadState[F[_]: Monad, E, L, S](
    implicit ev: Monad[IndexedReaderWriterStateT[F, E, L, S, S, *]],
    monoid: Monoid[L]
  ): Stateful[IndexedReaderWriterStateT[F, E, L, S, S, *], S] =
    new Stateful[IndexedReaderWriterStateT[F, E, L, S, S, *], S] {
      override val monad: Monad[IndexedReaderWriterStateT[F, E, L, S, S, *]] = ev
      override def get: IndexedReaderWriterStateT[F, E, L, S, S, S]          = IndexedReaderWriterStateT.get
      override def set(s: S): IndexedReaderWriterStateT[F, E, L, S, S, Unit] = IndexedReaderWriterStateT.set(s)
    }
}
