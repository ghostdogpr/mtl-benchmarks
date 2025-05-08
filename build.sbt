name := "ReaderWriterStateBenchmarks"

version := "0.1"

scalaVersion := "3.7.0"

libraryDependencies += "org.typelevel"      %% "cats-core"      % "2.13.0"
libraryDependencies += "org.typelevel"      %% "cats-effect"    % "3.6.1"
libraryDependencies += "org.typelevel"      %% "cats-mtl"       % "1.5.0"
libraryDependencies += "dev.zio"            %% "zio"            % "2.1.17"
libraryDependencies += "dev.zio"            %% "zio-prelude"    % "1.0.0-RC39"
libraryDependencies += "io.getkyo"          %% "kyo-core"       % "0.18.0"
libraryDependencies += "io.github.marcinzh" %% "turbolift-core" % "0.112.0"

scalacOptions += "-Xkind-projector"
scalacOptions += "-preview"

resolvers ++= Resolver.sonatypeOssRepos("snapshots")

enablePlugins(JmhPlugin)
