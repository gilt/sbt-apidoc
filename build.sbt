name := "sbt-apidoc"

organization := "com.gilt.sbt"

sbtPlugin := true

scalaVersion := "2.10.4"

crossScalaVersions in ThisBuild := Seq("2.10.4")

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.3.4",
  "com.ning" % "async-http-client" % "1.8.13"
)


// TODO publish
