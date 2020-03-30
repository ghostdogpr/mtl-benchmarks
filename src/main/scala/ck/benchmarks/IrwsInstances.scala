package ck.benchmarks

import cats.{ Applicative, Functor, Monad }
import cats.data.IndexedReaderWriterStateT
import cats.effect.SyncIO
import cats.kernel.Monoid
import cats.mtl._

object IrwsInstances {

  implicit def irwsApplicativeAsk[E, L, S](
    implicit ev: Applicative[IndexedReaderWriterStateT[SyncIO, E, L, S, S, *]],
    monoid: Monoid[L]
  ): ApplicativeAsk[IndexedReaderWriterStateT[SyncIO, E, L, S, S, *], E] =
    new DefaultApplicativeAsk[IndexedReaderWriterStateT[SyncIO, E, L, S, S, *], E] {
      override val applicative: Applicative[IndexedReaderWriterStateT[SyncIO, E, L, S, S, *]] = ev
      override def ask: IndexedReaderWriterStateT[SyncIO, E, L, S, S, E]                      = IndexedReaderWriterStateT.ask
    }

  implicit def irwsFunctorTell[E, L, S](
    implicit ev: Functor[IndexedReaderWriterStateT[SyncIO, E, L, S, S, *]]
  ): FunctorTell[IndexedReaderWriterStateT[SyncIO, E, L, S, S, *], L] =
    new DefaultFunctorTell[IndexedReaderWriterStateT[SyncIO, E, L, S, S, *], L] {
      override val functor: Functor[IndexedReaderWriterStateT[SyncIO, E, L, S, S, *]] = ev
      override def tell(l: L): IndexedReaderWriterStateT[SyncIO, E, L, S, S, Unit]    = IndexedReaderWriterStateT.tell(l)
    }

  implicit def irwsMonadState[E, L, S](
    implicit ev: Monad[IndexedReaderWriterStateT[SyncIO, E, L, S, S, *]],
    monoid: Monoid[L]
  ): MonadState[IndexedReaderWriterStateT[SyncIO, E, L, S, S, *], S] =
    new DefaultMonadState[IndexedReaderWriterStateT[SyncIO, E, L, S, S, *], S] {
      override val monad: Monad[IndexedReaderWriterStateT[SyncIO, E, L, S, S, *]] = ev
      override def get: IndexedReaderWriterStateT[SyncIO, E, L, S, S, S]          = IndexedReaderWriterStateT.get
      override def set(s: S): IndexedReaderWriterStateT[SyncIO, E, L, S, S, Unit] = IndexedReaderWriterStateT.set(s)
    }
}
