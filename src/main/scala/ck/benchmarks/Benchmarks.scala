package ck.benchmarks

import java.util.concurrent.TimeUnit
import scala.language.{ higherKinds, postfixOps }
import cats.effect.IO
import ck.benchmarks.IrwsInstances._
import ck.benchmarks.Test._
import ck.benchmarks.ZioInstances._
import org.openjdk.jmh.annotations.{ State => S, _ }
import zio.internal.Platform
import zio.{ BootstrapRuntime, Ref, Runtime, ZEnv }

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

  @Benchmark
  def simpleReaderWriterState(): Unit =
    testReaderWriterState[IO].run(Env("config"), State(2)).unsafeRunSync()

  @Benchmark
  def simpleMTLZIO(): Unit = {
    runtime.unsafeRun(for {
      s <- Ref.make(State(2))
      e = Env("config")
      _ <- testMTL[P].provide(new ZIOEnv[Env, State] {
            override def env: Env          = e
            override def state: Ref[State] = s
          })
    } yield ())
    ()
  }

  @Benchmark
  def simpleMTLReaderWriterState(): Unit =
    testMTL[P2].run(Env("config"), State(2)).unsafeRunSync()

}
