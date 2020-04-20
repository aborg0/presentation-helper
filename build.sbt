name := "presentation-helper"

version := "0.1"

scalaVersion := "2.13.1"

libraryDependencies += "org.scalafx" %% "scalafx" % "12.0.2-R18"

// Determine OS version of JavaFX binaries
lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux") => "linux"
  case n if n.startsWith("Mac") => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}

// Add JavaFX dependencies
lazy val javaFXModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
libraryDependencies ++= javaFXModules.map( m=>
  "org.openjfx" % s"javafx-$m" % "12.0.2" classifier osName
)


// https://mvnrepository.com/artifact/org.eclipse.jgit/org.eclipse.jgit
libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit" % "5.7.0.202003110725-r"

libraryDependencies += "com.github.pureconfig" %% "pureconfig" % "0.12.3"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.1.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.1" % "test"

assembly / mainClass := Some("com.github.aborg0.presentation_helper.App")
assembly / assemblyMergeStrategy := {
  case "module-info.class" => MergeStrategy.discard
  case PathList(ps @ _*) if ps.last == "module-info.class" => MergeStrategy.discard
case "META-INF/versions/9/module-info.class" => MergeStrategy.discard
case x =>
  val oldStrategy = (assemblyMergeStrategy in assembly).value
  oldStrategy(x)
}