ThisBuild / scalaVersion := "3.1.0"
ThisBuild / publishTo := Some( Resolver.file( "file",  new File("/var/www/maven" ) ) )
ThisBuild / resolvers += "ai.dragonfly.code" at "https://code.dragonfly.ai/"
ThisBuild / organization := "ai.dragonfly.code"
ThisBuild / scalacOptions ++= Seq("-feature", "-deprecation")
ThisBuild / version := "0.01"

lazy val beacon = project.enablePlugins(ScalaJSPlugin).settings(
  name := "beacon",
  libraryDependencies ++= Seq(
    "ai.dragonfly.code" %%% "bitfrost" % "0.0.01",
    "com.lihaoyi" %%% "scalatags" % "0.11.1"
  )
)

lazy val ui = project.enablePlugins(ScalaJSPlugin).dependsOn(beacon).settings(
  name := "ui",
  Compile / fastOptJS / artifactPath := file("./public_html/js/ui.js"),
  Compile / fullOptJS / artifactPath := file("./public_html/js/ui.js"),
  Compile / mainClass := Some("Main"),
  scalaJSUseMainModuleInitializer := true
)

lazy val worker = project.enablePlugins(ScalaJSPlugin).dependsOn(beacon).settings(
  name := "worker",
  Compile / fastOptJS / artifactPath := file("./public_html/js/worker.js"),
  Compile / fullOptJS / artifactPath := file("./public_html/js/worker.js"),
  Compile / mainClass := Some("Main"),
  scalaJSUseMainModuleInitializer := true
)