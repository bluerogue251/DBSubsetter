name := "DBSubsetter"

version := "0.1"

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  "com.github.scopt" %% "scopt" % "3.7.0",
  "com.typesafe.akka" %% "akka-stream" % "2.5.6",
  "org.postgresql" % "postgresql" % "42.1.4",
  "mysql" % "mysql-connector-java" % "8.0.8-dmr",
  "org.scalatest" %% "scalatest" % "3.0.4" % "test"
)