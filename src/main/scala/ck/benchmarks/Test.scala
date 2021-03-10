package ck.benchmarks

import cats.{ Eval, Monad }
import cats.data.{ Chain, IndexedReaderWriterStateT }
import cats.effect.SyncIO
import cats.implicits._
import cats.mtl._
import ck.benchmarks.ZioInstances.ZIOReaderWriterState
import zio.prelude.fx.ZPure

object Test {
  case class Env(config: String)
  case class Event(name: String)
  case class State(value: Int)

  type P[+A] = ZIOReaderWriterState[Env, Chain[Event], State, A]
  type P2[A] = IndexedReaderWriterStateT[SyncIO, Env, Chain[Event], State, State, A]
  type P3[A] = IndexedReaderWriterStateT[Eval, Env, Chain[Event], State, State, A]
  type P4[A] = ZPure[Chain[Event], State, State, Env, Nothing, A]

  val loops = 1000

  def testReaderWriterState[F[_]: Monad]: IndexedReaderWriterStateT[F, Env, Chain[Event], State, State, Unit] =
    (1 to loops).toList
      .map(_ =>
        for {
          conf <- IndexedReaderWriterStateT.ask[F, Env, Chain[Event], State].map(_.config)
          _    <- IndexedReaderWriterStateT.tell[F, Env, Chain[Event], State](Chain(Event(s"Env = $conf")))
          _ <- IndexedReaderWriterStateT.modify[F, Env, Chain[Event], State, State](state =>
                state.copy(value = state.value + 1)
              )
        } yield ()
      )
      .sequence
      .void

  def testZPure: ZPure[Chain[Event], State, State, Env, Nothing, Unit] =
    ZPure
      .forEach((1 to loops).toList)(_ =>
        for {
          conf <- ZPure.access[Env](_.config)
          _    <- ZPure.log(Chain(Event(s"Env = $conf")))
          _    <- ZPure.update[State, State](state => state.copy(value = state.value + 1))
        } yield ()
      )
      .unit

  def testMTL[F[_]: Monad](
    implicit reader: Ask[F, Env],
    writer: Tell[F, Chain[Event]],
    state: Stateful[F, State]
  ): F[Unit] =
    (1 to loops).toList
      .map(_ =>
        for {
          conf <- reader.ask.map(_.config)
          _    <- writer.tell(Chain(Event(s"Env = $conf")))
          _    <- state.modify(state => state.copy(value = state.value + 1))
        } yield ()
      )
      .sequence
      .void
}
