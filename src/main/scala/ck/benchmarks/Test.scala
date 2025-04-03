package ck.benchmarks

import cats.Monad
import cats.data.{Chain, IndexedReaderWriterStateT}
import cats.effect.SyncIO
import cats.syntax.all.*
import cats.mtl.*
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

  import turbolift.!!
  import turbolift.effects.{Reader, WriterK}
  import turbolift.Extensions.*
  import turbolift.typeclass.AccumZero

  case object MyReader extends Reader[Environment]
  case object MyWriter extends WriterK[Chain, Event]
  case object MyState  extends turbolift.effects.State[State]
  type MyReader = MyReader.type
  type MyWriter = MyWriter.type
  type MyState  = MyState.type

  given [A]: AccumZero[Chain[A], A] = new AccumZero[Chain[A], A] {
    def zero: Chain[A]                           = Chain.empty
    def one(a: A): Chain[A]                      = Chain.one(a)
    def plus(a: Chain[A], b: Chain[A]): Chain[A] = a ++ b
    def plus1(a: Chain[A], b: A): Chain[A]       = a :+ b
  }

  def testTurboLift: Unit !! (MyState & MyWriter & MyReader) =
    loops
      .map(_ =>
        for {
          conf <- MyReader.asks(_.config)
          event = Event(s"Env = $conf")
          _    <- MyWriter.tell(event)
          add   = 1
          _    <- MyState.modify(state => state.copy(value = state.value + add))
        } yield ()
      )
      .traverseVoid
}
