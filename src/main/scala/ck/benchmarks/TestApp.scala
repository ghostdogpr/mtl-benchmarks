package ck.benchmarks

import cats.data.Chain
import ck.benchmarks.Test._
import ck.benchmarks.ZPureInstances._
import kyo._
import zio.Chunk

object TestApp extends App {
  println(
    Aborts[Throwable].run(
      Vars[State].run(State(2))(
        Sums[Chunk[Event]].run(
          Envs[Env].run(Env("config"))(
            testKyo.andThen(Vars[State].get)
          )
        )
      )
    ).pure
  )
//  println(testZPure.provideService(Env("config")).runAll(State(2)))
//  println(testMTLChunk[P4].provideService(Env("config")).runAll(State(2)))
//  println(testMTL[P3].run(Env("config"), State(2)))
}
