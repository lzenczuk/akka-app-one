name := """akka-app-one"""

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.9",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.9" % "test",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "com.typesafe.akka" %% "akka-persistence" % "2.4.9",
  "org.iq80.leveldb" % "leveldb" % "0.7",
  "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",
  "com.typesafe.akka" %% "akka-persistence-query-experimental" % "2.4.9",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.9",
  "org.apache.commons" % "commons-compress" % "1.12"
)

// define main class to run
mainClass in (Compile, run) := Some("com.github.lzenczuk.akka.course.CourseMain")

// define main class in jar's MANIFEST.MF
mainClass in (Compile, packageBin) := Some("com.github.lzenczuk.akka.course.CourseMain")
