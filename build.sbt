name := "ReaderWriterStateBenchmarks"

version := "0.1"

scalaVersion := "2.13.7"

libraryDependencies += "org.typelevel" %% "cats-core"   % "2.9.0"
libraryDependencies += "org.typelevel" %% "cats-effect" % "3.4.8"
libraryDependencies += "org.typelevel" %% "cats-mtl"    % "1.3.0"
libraryDependencies += "dev.zio"       %% "zio"         % "2.0.13"
libraryDependencies += "dev.zio"       %% "zio-prelude" % "1.0.0-RC21"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.2" cross CrossVersion.full)
addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")

enablePlugins(JmhPlugin)
