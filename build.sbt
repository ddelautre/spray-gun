lazy val projectSettings = Seq(
  name := "spray-gun",
  version := "1.1.1",
  scalaVersion := "2.11.5"
)

lazy val dependencies = Seq(
  "io.spray" %% "spray-can" % "1.3.2",
  "com.typesafe.akka" %% "akka-actor" % "2.3.9",
  "org.scalatest" %% "scalatest" % "2.2.1" % Test,
  "com.jayway.restassured" % "rest-assured" % "2.4.0" % Test
)

lazy val sprayGun = (project in file("."))
  .settings(projectSettings: _*)
  .settings(libraryDependencies ++= dependencies)
