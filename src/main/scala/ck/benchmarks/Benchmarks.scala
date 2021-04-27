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
import org.openjdk.jmh.annotations.{ State => S, _ }
import zio.internal.Platform
import zio.{ BootstrapRuntime, Ref, Runtime, ZEnv, ZLayer }

@S(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(1)
class Benchmarks {

  private val runtime: Runtime[ZEnv] = new BootstrapRuntime {
    override val platform: Platform = Platform.benchmark
  }

  private val layer =
    Ref.make(State(2)).toLayer ++
      Ref.make(Chain.empty[Event]).toLayer ++
      ZLayer.succeed(Env("config"))

  @Benchmark
  def readerWriterStateIO(): Unit =
    testReaderWriterState[IO].run(Env("config"), State(2)).unsafeRunSync()

  @Benchmark
  def ZPure(): Unit =
    testZPure.provide(Env("config")).run(State(2))

  @Benchmark
  def MTLZIO(): Unit =
    runtime.unsafeRun(testMTL[P].provideLayer(layer))

  @Benchmark
  def MTLZPure(): Unit =
    testMTLChunk[P4].provide(Env("config")).runAll(State(2))

  @Benchmark
  def MTLReaderWriterStateIO(): Unit =
    testMTL[P2].run(Env("config"), State(2)).unsafeRunSync()

  @Benchmark
  def MTLReaderWriterStateEither(): Unit =
    testMTL[P3].run(Env("config"), State(2))
}
