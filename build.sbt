name := """video-quiz"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  "com.mohiva" %% "play-silhouette" % "3.0.0",
  "com.typesafe.slick" %% "slick" % "3.0.2",
  "com.typesafe.slick" %% "slick-codegen" % "3.0.2",
  "com.typesafe.play" %% "play-slick" % "1.0.1",
  "com.typesafe.play" %% "play-slick-evolutions" % "1.0.1",
  "mysql" % "mysql-connector-java" % "5.1.36",
  "net.codingwell" %% "scala-guice" % "4.0.0",
  "net.ceedubs" %% "ficus" % "1.1.2",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scalatestplus" %% "play" % "1.2.0" % "test"
)++ webjars

lazy val webjars = Seq(
  "org.webjars.bower" % "bootstrap" % "3.3.5",
  "org.webjars.bower" % "bootstrap-material-design" % "0.3.0"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

routesGenerator := InjectedRoutesGenerator
pipelineStages := Seq(digest, gzip)

// slick code gen
Slick.slick <<= Slick.slickCodeGenTask

fork in run := true

scalaVersion := "2.11.7"
scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint"
)