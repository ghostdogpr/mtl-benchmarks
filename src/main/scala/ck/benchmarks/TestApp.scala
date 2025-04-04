package ck.benchmarks

import cats.data.Chain
import ck.benchmarks.Test.{*, given}
import ck.benchmarks.ZPureInstances.*
import kyo.*

object TestApp extends App {
  println(
    testTurboLift
      .handleWith(MyReader.handler(Environment("config")))
      .handleWith(MyState.handler(State(2)))
      .handleWith(MyWriter.handler)
      .handleWith(MyError.handler)
      .run
  )
//  println(
//    Abort
//      .run(
//        Var.run(State(2))(
//          Emit.run(
//            Env.run(Environment("config"))(
//              testKyo.andThen(Var.get[State])
//            )
//          )
//        )
//      )
//      .eval
//  )
//  println(testZPure.provideService(Env("config")).runAll(State(2)))
//  println(testMTLChunk[P4].provideService(Env("config")).runAll(State(2)))
//  println(testMTL[P3].run(Env("config"), State(2)))
}
