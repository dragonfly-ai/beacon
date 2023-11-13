ThisBuild / scalaVersion := "3.2.1"
ThisBuild / publishTo := Some( Resolver.file( "file",  new File("/var/www/maven" ) ) )
ThisBuild / resolvers += "ai.dragonfly.code" at "https://code.dragonfly.ai/"
ThisBuild / organization := "ai.dragonfly.code"
ThisBuild / scalacOptions ++= Seq("-feature", "-deprecation")
ThisBuild / version := "0.02"

lazy val beacon = project.enablePlugins(ScalaJSPlugin).settings(
  name := "beacon",
  libraryDependencies ++= Seq(
    "ai.dragonfly.code" %%% "bitfrost" % "0.0.02",
    "com.lihaoyi" %%% "scalatags" % "0.12.0"
  )
)

lazy val ui = project.enablePlugins(ScalaJSPlugin).dependsOn(beacon).settings(
  name := "ui",
  Compile / fastOptJS / artifactPath := file("./docs/js/ui.js"),
  Compile / fullOptJS / artifactPath := file("./docs/js/ui.js"),
  Compile / mainClass := Some("Main"),
  scalaJSUseMainModuleInitializer := true
)

lazy val worker = project.enablePlugins(ScalaJSPlugin).dependsOn(beacon).settings(
  name := "worker",
  Compile / fastOptJS / artifactPath := file("./docs/js/worker.js"),
  Compile / fullOptJS / artifactPath := file("./docs/js/worker.js"),
  Compile / mainClass := Some("Main"),
  scalaJSUseMainModuleInitializer := true
)