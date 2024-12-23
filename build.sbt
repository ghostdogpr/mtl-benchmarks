name := "ReaderWriterStateBenchmarks"

version := "0.1"

scalaVersion := "3.5.2"

libraryDependencies += "org.typelevel" %% "cats-core"   % "2.12.0"
libraryDependencies += "org.typelevel" %% "cats-effect" % "3.5.7"
libraryDependencies += "org.typelevel" %% "cats-mtl"    % "1.5.0"
libraryDependencies += "dev.zio"       %% "zio"         % "2.1.14"
libraryDependencies += "dev.zio"       %% "zio-prelude" % "1.0.0-RC35"
libraryDependencies += "io.getkyo"     %% "kyo-core"    % "0.15.1"

scalacOptions += "-Xkind-projector"

resolvers ++= Resolver.sonatypeOssRepos("snapshots")

enablePlugins(JmhPlugin)
