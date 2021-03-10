package ck.benchmarks

import ck.benchmarks.Test.{ testMTL, testZPure, Env, P4, State }
import ck.benchmarks.ZPureInstances._

object TestApp extends App {
  println(testZPure.provide(Env("config")).run(State(2)))
  println(testMTL[P4].provide(Env("config")).run(State(2)))
}
