name := "ReaderWriterStateBenchmarks"

version := "0.1"

scalaVersion := "3.6.4"

libraryDependencies += "org.typelevel" %% "cats-core"   % "2.13.0"
libraryDependencies += "org.typelevel" %% "cats-effect" % "3.6.0"
libraryDependencies += "org.typelevel" %% "cats-mtl"    % "1.5.0"
libraryDependencies += "dev.zio"       %% "zio"         % "2.1.16"
libraryDependencies += "dev.zio"       %% "zio-prelude" % "1.0.0-RC39"
libraryDependencies += "io.getkyo"     %% "kyo-core"    % "0.17.0"

scalacOptions += "-Xkind-projector"
scalacOptions += "-experimental"
scalacOptions += "-language:experimental.betterFors"

resolvers ++= Resolver.sonatypeOssRepos("snapshots")

enablePlugins(JmhPlugin)
