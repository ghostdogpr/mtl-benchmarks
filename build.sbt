name := "ReaderWriterStateBenchmarks"

version := "0.1"

scalaVersion := "2.12.11"

scalacOptions += "-Ypartial-unification"

libraryDependencies += "org.typelevel" %% "cats-core"     % "2.1.0"
libraryDependencies += "org.typelevel" %% "cats-effect"   % "2.1.2"
libraryDependencies += "org.typelevel" %% "cats-mtl-core" % "0.7.1"
libraryDependencies += "dev.zio"       %% "zio"           % "1.0.0-RC18-2"

addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.11.0" cross CrossVersion.full)
addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")

enablePlugins(JmhPlugin)
