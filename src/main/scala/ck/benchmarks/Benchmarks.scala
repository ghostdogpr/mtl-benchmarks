package ck.benchmarks

import java.util.concurrent.TimeUnit

import scala.language.postfixOps

import cats.data.Chain
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import ck.benchmarks.IrwsInstances._
import ck.benchmarks.Test._
import ck.benchmarks.ZioInstances._
import ck.benchmarks.ZPureInstances._
import kyo.{Env => KEnv, _}
import org.openjdk.jmh.annotations.{State => S, _}
import zio.{Chunk, Ref, ZLayer}

@S(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(1)
class Benchmarks {

  private val layer =
    ZLayer(Ref.make(State(2))) ++
      ZLayer(Ref.make(Chain.empty[Event])) ++
      ZLayer.succeed(Environment("config"))

  @Benchmark
  def readerWriterStateIO(): Unit =
    testReaderWriterState[IO].run(Environment("config"), State(2)).unsafeRunSync()

  @Benchmark
  def ZPure(): Unit =
    testZPure.provideService(Environment("config")).runAll(State(2))

  @Benchmark
  def kyo(): Unit =
    Abort
      .run(
        Var.run(State(2))(
          Emit.run(
            KEnv.run(Environment("config"))(
              testKyo.andThen(Var.get[State])
            )
          )
        )
      )
      .eval

  @Benchmark
  def MTLZIO(): Unit =
    zio.Unsafe.unsafe(implicit u =>
      zio.Runtime.default.unsafe.run(testMTL[P].provideLayer(layer)).getOrThrowFiberFailure()
    )

  @Benchmark
  def MTLZPure(): Unit =
    testMTLChunk[P4].provideService(Environment("config")).runAll(State(2))

  @Benchmark
  def MTLReaderWriterStateIO(): Unit =
    testMTL[P2].run(Environment("config"), State(2)).unsafeRunSync()

  @Benchmark
  def MTLReaderWriterStateEither(): Unit =
    testMTL[P3].run(Environment("config"), State(2))
}
