package ck.benchmarks

import cats.Monad
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
  type P3[A] = IndexedReaderWriterStateT[Either[Throwable, *], Env, Chain[Event], State, State, A]
  type P4[A] = ZPure[Event, State, State, Env, Throwable, A]

  val loops = (1 to 1000).toList

  def testReaderWriterState[F[_]: Monad]: IndexedReaderWriterStateT[F, Env, Chain[Event], State, State, Unit] =
    loops
      .traverse_(_ =>
        for {
          conf <- IndexedReaderWriterStateT.ask[F, Env, Chain[Event], State].map(_.config)
          _    <- IndexedReaderWriterStateT.tell[F, Env, Chain[Event], State](Chain(Event(s"Env = $conf")))
          _ <- IndexedReaderWriterStateT.modify[F, Env, Chain[Event], State, State](state =>
                state.copy(value = state.value + 1)
              )
        } yield ()
      )

  def testZPure: ZPure[Event, State, State, Env, Throwable, Unit] =
    ZPure.foreachDiscard(loops)(_ =>
      for {
        conf <- ZPure.serviceWith[Env](_.config)
        _    <- ZPure.log(Event(s"Env = $conf"))
        _    <- ZPure.update[State, State](state => state.copy(value = state.value + 1))
      } yield ()
    )

  def testMTL[F[_]: Monad](
    implicit reader: Ask[F, Env],
    writer: Tell[F, Chain[Event]],
    state: Stateful[F, State]
  ): F[Unit] =
    loops.traverse_(_ =>
      for {
        conf <- reader.ask.map(_.config)
        _    <- writer.tell(Chain(Event(s"Env = $conf")))
        _    <- state.modify(state => state.copy(value = state.value + 1))
      } yield ()
    )

  def testMTLChunk[F[_]: Monad](
    implicit reader: Ask[F, Env],
    writer: Tell[F, Event],
    state: Stateful[F, State]
  ): F[Unit] =
    loops.traverse_(_ =>
      for {
        conf <- reader.ask.map(_.config)
        _    <- writer.tell(Event(s"Env = $conf"))
        _    <- state.modify(state => state.copy(value = state.value + 1))
      } yield ()
    )

  import kyo._

  def testKyo: Unit < (Aborts[Throwable] & Envs[Env] & Vars[State | Chain[Event]]) =
    Seqs.traverseUnit(loops)(_ =>
      for {
        conf <- Envs[Env].use(_.config)
        _    <- Vars.update[Chain[Event]](_ :+ Event(s"Env = $conf"))
        _    <- Vars.update[State](state => state.copy(value = state.value + 1))
      } yield ()
    )
}
