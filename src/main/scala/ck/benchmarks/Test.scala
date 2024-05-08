package ck.benchmarks

import cats.Monad
import cats.data.{Chain, IndexedReaderWriterStateT}
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

  private val loops = (1 to 1000).toList

  def testReaderWriterState[F[_]: Monad]: IndexedReaderWriterStateT[F, Env, Chain[Event], State, State, Unit] =
    loops
      .traverse_(_ =>
        for {
          conf <- IndexedReaderWriterStateT.ask[F, Env, Chain[Event], State].map(_.config)
          event = Event(s"Env = $conf")
          _    <- IndexedReaderWriterStateT.tell[F, Env, Chain[Event], State](Chain(event))
          add   = 1
          _    <- IndexedReaderWriterStateT.modify[F, Env, Chain[Event], State, State](state =>
                    state.copy(value = state.value + add)
                  )
        } yield ()
      )

  def testZPure: ZPure[Event, State, State, Env, Throwable, Unit] =
    ZPure.foreachDiscard(loops)(_ =>
      for {
        conf <- ZPure.serviceWith[Env](_.config)
        event = Event(s"Env = $conf")
        _    <- ZPure.log(event)
        add   = 1
        _    <- ZPure.update[State, State](state => state.copy(value = state.value + add))
      } yield ()
    )

  def testMTL[F[_]: Monad](implicit
      reader: Ask[F, Env],
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
      reader: Ask[F, Env],
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

  def testKyo: Unit < (Sums[Event] & Vars[State] & Envs[Env] & Aborts[Throwable]) =
    Seqs.foreach(loops)(_ =>
      for {
        conf <- Envs.use[Env](_.config)
        event = Event(s"Env = $conf")
        _    <- Sums.add(event)
        add   = 1
        _    <- Vars.update[State](state => state.copy(value = state.value + add))
      } yield ()
    )
}
