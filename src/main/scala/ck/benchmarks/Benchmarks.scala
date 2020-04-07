package ck.benchmarks

import java.util.concurrent.TimeUnit
import scala.language.{ higherKinds, postfixOps }
import cats.data.Chain
import cats.effect.IO
import ck.benchmarks.IrwsInstances._
import ck.benchmarks.Test._
import ck.benchmarks.ZioInstances._
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
    ZLayer.fromEffect(Ref.make(State(2))) ++
      ZLayer.fromEffect(Ref.make(Chain.empty[Event])) ++
      ZLayer.succeed(Env("config"))

  @Benchmark
  def readerWriterStateIO(): Unit =
    testReaderWriterState[IO].run(Env("config"), State(2)).unsafeRunSync()

  @Benchmark
  def MTLZIO(): Unit =
    runtime.unsafeRun(testMTL[P].provideLayer(layer))

  @Benchmark
  def MTLReaderWriterStateIO(): Unit =
    testMTL[P2].run(Env("config"), State(2)).unsafeRunSync()

  @Benchmark
  def MTLReaderWriterStateEval(): Unit =
    testMTL[P3].run(Env("config"), State(2)).value
}
