name := "DBSubsetter"

version := "v1.0.0-beta.3"

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  // Command line parser
  "com.github.scopt" %% "scopt" % "3.7.0",

  // Akka stream processing
  "com.typesafe.akka" %% "akka-stream" % "2.5.8",

  // JDBC Drivers
  "org.postgresql" % "postgresql" % "42.1.4",
  "mysql" % "mysql-connector-java" % "8.0.8-dmr",
  "com.microsoft.sqlserver" % "mssql-jdbc" % "6.2.1.jre8",

  // Off-heap data structures
  "net.openhft" % "chronicle-queue" % "4.6.57",

  // For testing only
  "org.scalatest" %% "scalatest" % "3.0.4" % "test",
  "com.typesafe.slick" %% "slick" % "3.2.1" % "test",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.1" % "test",
  "org.slf4j" % "slf4j-nop" % "1.6.4" % "test"
)