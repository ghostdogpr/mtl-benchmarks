package ck.benchmarks

import ck.benchmarks.Test._
import ck.benchmarks.ZPureInstances._

object TestApp extends App {
  println(testZPure.provide(Env("config")).run(State(2)))
  println(testMTLChunk[P4].provide(Env("config")).run(State(2)))
}
