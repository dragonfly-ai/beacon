ThisBuild / scalaVersion := "3.3.6"
ThisBuild / publishTo := Some( Resolver.file( "file",  new File("/var/www/maven" ) ) )
ThisBuild / organization := "ai.dragonfly"
ThisBuild / organizationName := "dragonfly.ai"
ThisBuild / scalacOptions ++= Seq("-feature", "-deprecation")
ThisBuild / version := "0.1"

lazy val beacon = project.enablePlugins(ScalaJSPlugin).settings(
  name := "beacon",
  libraryDependencies ++= Seq(
    "ai.dragonfly" %%% "uriel" % "0.11",
    "com.lihaoyi" %%% "scalatags" % "0.13.1"
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

lazy val root = beacon.settings(name := "beacon")