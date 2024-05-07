package ck.benchmarks

import cats.data.Chain
import ck.benchmarks.Test._
import ck.benchmarks.ZPureInstances._
import kyo._

object TestApp extends App {
  println(
    Aborts
      .run(
        Vars.run(State(2))(
          Sums.run(
            Envs.run(Env("config"))(
              testKyo.andThen(Vars.get)
            )
          )
        )
      )
      .pure
  )
//  println(testZPure.provideService(Env("config")).runAll(State(2)))
//  println(testMTLChunk[P4].provideService(Env("config")).runAll(State(2)))
//  println(testMTL[P3].run(Env("config"), State(2)))
}
