ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "purecube",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.7.0",
      "org.typelevel" %% "cats-effect" % "3.3.5",
      "co.fs2" %% "fs2-core" % "3.2.5",
      "io.netty" % "netty-all" % "4.1.74.Final",
    )
  )
