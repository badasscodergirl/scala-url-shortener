name := """scala-url-shortener"""
organization := "com.bcg"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.8"

libraryDependencies += guice
libraryDependencies ++= Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.2" % Test,
  "joda-time" % "joda-time" % "2.8.1" withSources(),

  "org.postgresql" % "postgresql" % "42.2.5",
  "com.typesafe.slick" %% "slick" % "3.2.3" withSources(),
  "com.typesafe.play" %% "play-slick" % "3.0.0" withSources(),

  "com.typesafe.play" %% "play-json" % "2.7.0" withSources()

)

scalacOptions ++= Seq("-deprecation", "-feature")

