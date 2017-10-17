import com.darkyen.resourcepacker.{PackingOperation, PackingOperationKt}

name := "Fountain Sim"
version := "1.1"

kotlinLib("stdlib")

crossPaths := false

autoScalaLibrary := false

val gdxVersion = "1.9.5"

resolvers += "jitpack" at "https://jitpack.io"

// Core
libraryDependencies ++= Seq(
  "com.badlogicgames.gdx" % "gdx" % gdxVersion,
  "com.badlogicgames.gdx" % "gdx-box2d" % gdxVersion
)

//Desktop
libraryDependencies ++= Seq(
  "com.badlogicgames.gdx" % "gdx-backend-lwjgl3" % gdxVersion,
  "com.badlogicgames.gdx" % "gdx-platform" % gdxVersion classifier "natives-desktop",
  "com.badlogicgames.gdx" % "gdx-box2d-platform" % gdxVersion classifier "natives-desktop"
)

javacOptions ++= Seq("-g", "-Xlint", "-Xlint:-rawtypes", "-Xlint:-unchecked")

javaOptions ++= Seq("-ea")

import scala.collection.JavaConverters._

TaskKey[Unit]("packResources") := {
  val settings = Seq(PackingOperationKt.getPreferSymlinks.to(true))
  ResourcePacker.resourcePack(new PackingOperation("./resources", "./src/main/resources", settings.asJava))
}

mainClass in assembly := Some("com.darkyen.MainKt")

assemblyJarName in assembly := "FountainSim-"+version.value
