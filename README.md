# beacon
An application of Dynamic Programming, Octree search, Perceptual Color Models, and Web Workers for designing Beacon Colors for Minecraft.

<img src="https://raw.githubusercontent.com/dragonfly-ai/beacon/main/public_html/image/screenshot01.png">

If you want to design minecraft beacon colors without building this project from source code, you can find a current version of the app here: https://colortree.net/beacon/index.html


To build from source, use sbt: `fastOptJS`

To use beacon as a library in another project:
```scala
resolvers += "dragonfly.ai" at "https://code.dragonfly.ai/"
libraryDependencies ++= Seq(
  "ai.dragonfly.code" %%% "beacon" % "0.02",
  "ai.dragonfly.code" %%% "ui" % "0.02",
  "ai.dragonfly.code" %%% "worker" % "0.02"
)
```