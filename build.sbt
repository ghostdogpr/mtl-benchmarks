name := "ReaderWriterStateBenchmarks"

version := "0.1"

scalaVersion := "3.4.1"

libraryDependencies += "org.typelevel" %% "cats-core"   % "2.10.0"
libraryDependencies += "org.typelevel" %% "cats-effect" % "3.4.8"
libraryDependencies += "org.typelevel" %% "cats-mtl"    % "1.4.0"
libraryDependencies += "dev.zio"       %% "zio"         % "2.0.21"
libraryDependencies += "dev.zio"       %% "zio-prelude" % "1.0.0-RC21"
libraryDependencies += "io.getkyo"     %% "kyo-core"    % "0.9.3"

scalacOptions += "-Ykind-projector"

resolvers ++= Resolver.sonatypeOssRepos("snapshots")

enablePlugins(JmhPlugin)
