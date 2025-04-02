package ck.benchmarks

import cats.Monad
import cats.data.{Chain, IndexedReaderWriterStateT}
import cats.effect.SyncIO
import cats.implicits._
import cats.mtl._
import ck.benchmarks.ZioInstances.ZIOReaderWriterState
import zio.prelude.fx.ZPure

object Test {
  case class Environment(config: String)
  case class Event(name: String)
  case class State(value: Int)

  type P[+A] = ZIOReaderWriterState[Environment, Chain[Event], State, A]
  type P2[A] = IndexedReaderWriterStateT[SyncIO, Environment, Chain[Event], State, State, A]
  type P3[A] = IndexedReaderWriterStateT[Either[Throwable, *], Environment, Chain[Event], State, State, A]
  type P4[A] = ZPure[Event, State, State, Environment, Throwable, A]

  private val loops = (1 to 1000).toList

  def testReaderWriterState[F[_]: Monad]: IndexedReaderWriterStateT[F, Environment, Chain[Event], State, State, Unit] =
    loops
      .traverse_(_ =>
        for {
          conf <- IndexedReaderWriterStateT.ask[F, Environment, Chain[Event], State].map(_.config)
          event = Event(s"Env = $conf")
          _    <- IndexedReaderWriterStateT.tell[F, Environment, Chain[Event], State](Chain(event))
          add   = 1
          _    <- IndexedReaderWriterStateT.modify[F, Environment, Chain[Event], State, State](state =>
                    state.copy(value = state.value + add)
                  )
        } yield ()
      )

  def testZPure: ZPure[Event, State, State, Environment, Throwable, Unit] =
    ZPure.foreachDiscard(loops)(_ =>
      for {
        conf <- ZPure.serviceWith[Environment](_.config)
        event = Event(s"Env = $conf")
        _    <- ZPure.log(event)
        add   = 1
        _    <- ZPure.update[State, State](state => state.copy(value = state.value + add))
      } yield ()
    )

  def testMTL[F[_]: Monad](implicit
      reader: Ask[F, Environment],
      writer: Tell[F, Chain[Event]],
      state: Stateful[F, State]
  ): F[Unit] =
    loops.traverse_(_ =>
      for {
        conf <- reader.ask.map(_.config)
        event = Event(s"Env = $conf")
        _    <- writer.tell(Chain(event))
        add   = 1
        _    <- state.modify(state => state.copy(value = state.value + add))
      } yield ()
    )

  def testMTLChunk[F[_]: Monad](implicit
      reader: Ask[F, Environment],
      writer: Tell[F, Event],
      state: Stateful[F, State]
  ): F[Unit] =
    loops.traverse_(_ =>
      for {
        conf <- reader.ask.map(_.config)
        event = Event(s"Env = $conf")
        _    <- writer.tell(event)
        add   = 1
        _    <- state.modify(state => state.copy(value = state.value + add))
      } yield ()
    )

  import kyo._

  def testKyo: Unit < (Emit[Event] & Var[State] & Env[Environment] & Abort[Throwable]) =
    Kyo.foreachDiscard(loops)(_ =>
      for {
        conf <- Env.use[Environment](_.config)
        event = Event(s"Env = $conf")
        _    <- Emit.value(event)
        add   = 1
        _    <- Var.update[State](state => state.copy(value = state.value + add))
      } yield ()
    )
}
