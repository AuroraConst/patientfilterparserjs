lazy val root = project
  .in(file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    inThisBuild(List(
      organization := "com.axiom",
      version      := "0.0.1-SNAPSHOT",
      scalaVersion := "3.5.2"
    )),
    name := "patientfilterparserjs",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "fastparse" % "3.1.1",
      "org.scala-js"  %%% "scalajs-dom"    % "2.2.0",
      "com.axiom"   %%% "dataimportcsv3s" % "0.0.1-SNAPSHOT",
      "org.scalatest" %%% "scalatest"      % "3.2.18"    % "test"
    ),
    scalaJSUseMainModuleInitializer := true
  )


// Automatically generate index-dev.html which uses *-fastopt.js
  Compile/resourceGenerators += Def.task {
  val source = ( Compile/resourceDirectory).value / "index.html"
  val target = (Compile/resourceManaged).value / "index-dev.html"

  val fullFileName = (Compile/fullOptJS/artifactPath).value.getName
  val fastFileName = (Compile/ fastOptJS/artifactPath).value.getName

  IO.writeLines(target,
    IO.readLines(source).map {
      line => line.replace(fullFileName, fastFileName)
    }
  )

  Seq(target)
}.taskValue
