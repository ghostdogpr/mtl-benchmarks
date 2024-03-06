package ck.benchmarks

import cats.data.Chain
import ck.benchmarks.Test._
import ck.benchmarks.ZPureInstances._
import kyo._

object TestApp extends App {
  println(
    Vars.run(
      Vars.let(State(2))(
        Vars.let(Chain.empty)(
          Envs[Env].run(Env("config"))(
            Aborts[Throwable].run(
              for {
                _      <- testKyo
                state  <- Vars.get[State]
                events <- Vars.get[Chain[Event]]
              } yield (events, state)
            )
          )
        )
      )
    )
  )
//  println(testZPure.provideService(Env("config")).runAll(State(2)))
//  println(testMTLChunk[P4].provideService(Env("config")).runAll(State(2)))
//  println(testMTL[P3].run(Env("config"), State(2)))
}
