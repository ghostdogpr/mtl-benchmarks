package ck.benchmarks

import scala.language.higherKinds
import cats.{ Applicative, Functor, Monad }
import cats.data.IndexedReaderWriterStateT
import cats.kernel.Monoid
import cats.mtl._

object IrwsInstances {

  implicit def irwsApplicativeAsk[F[_]: Applicative, E, L, S](
    implicit ev: Applicative[IndexedReaderWriterStateT[F, E, L, S, S, *]],
    monoid: Monoid[L]
  ): ApplicativeAsk[IndexedReaderWriterStateT[F, E, L, S, S, *], E] =
    new DefaultApplicativeAsk[IndexedReaderWriterStateT[F, E, L, S, S, *], E] {
      override val applicative: Applicative[IndexedReaderWriterStateT[F, E, L, S, S, *]] = ev
      override def ask: IndexedReaderWriterStateT[F, E, L, S, S, E]                      = IndexedReaderWriterStateT.ask
    }

  implicit def irwsFunctorTell[F[_]: Applicative, E, L, S](
    implicit ev: Functor[IndexedReaderWriterStateT[F, E, L, S, S, *]]
  ): FunctorTell[IndexedReaderWriterStateT[F, E, L, S, S, *], L] =
    new DefaultFunctorTell[IndexedReaderWriterStateT[F, E, L, S, S, *], L] {
      override val functor: Functor[IndexedReaderWriterStateT[F, E, L, S, S, *]] = ev
      override def tell(l: L): IndexedReaderWriterStateT[F, E, L, S, S, Unit]    = IndexedReaderWriterStateT.tell(l)
    }

  implicit def irwsMonadState[F[_]: Monad, E, L, S](
    implicit ev: Monad[IndexedReaderWriterStateT[F, E, L, S, S, *]],
    monoid: Monoid[L]
  ): MonadState[IndexedReaderWriterStateT[F, E, L, S, S, *], S] =
    new DefaultMonadState[IndexedReaderWriterStateT[F, E, L, S, S, *], S] {
      override val monad: Monad[IndexedReaderWriterStateT[F, E, L, S, S, *]] = ev
      override def get: IndexedReaderWriterStateT[F, E, L, S, S, S]          = IndexedReaderWriterStateT.get
      override def set(s: S): IndexedReaderWriterStateT[F, E, L, S, S, Unit] = IndexedReaderWriterStateT.set(s)
    }
}
