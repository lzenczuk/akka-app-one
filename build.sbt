name := """akka-app-one"""

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.11",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.11" % "test",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test")

// define main class to run
mainClass in (Compile, run) := Some("com.github.lzenczuk.akka.course.CourseMain")

// define main class in jar's MANIFEST.MF
mainClass in (Compile, packageBin) := Some("com.github.lzenczuk.akka.course.CourseMain")
