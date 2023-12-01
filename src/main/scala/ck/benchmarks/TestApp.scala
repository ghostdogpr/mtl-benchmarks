package ck.benchmarks

import ck.benchmarks.Test._
import ck.benchmarks.ZPureInstances._

object TestApp extends App {
  println(testZPure.provideService(Env("config")).runAll(State(2)))
  println(testMTLChunk[P4].provideService(Env("config")).runAll(State(2)))
  println(testMTL[P3].run(Env("config"), State(2)))
}
