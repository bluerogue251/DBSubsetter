name := "DBSubsetter"

version := "v1.0.0-beta.7"

scalaVersion := "2.12.3"

scalacOptions ++= Seq("-deprecation", "-Xfatal-warnings")

libraryDependencies ++= Seq(
  // Command line parser
  "com.github.scopt" %% "scopt" % "3.7.0",
  // JDBC drivers
  "org.postgresql" % "postgresql" % "42.1.4",
  "mysql" % "mysql-connector-java" % "8.0.8-dmr",
  "com.microsoft.sqlserver" % "mssql-jdbc" % "6.2.1.jre8",
  // Off-heap data structures
  "net.openhft" % "chronicle-queue" % "5.20.103",
  "org.rocksdb" % "rocksdbjni" % "6.14.6",
  // No-op logger to silence slf4j warnings
  "org.slf4j" % "slf4j-nop" % "1.7.25",
  // Observability tools
  "io.prometheus" % "simpleclient" % "0.6.0",
  "io.prometheus" % "simpleclient_hotspot" % "0.6.0",
  "io.prometheus" % "simpleclient_httpserver" % "0.6.0",
  // For testing only
  "org.scalatest" %% "scalatest" % "3.0.4" % "test",
  "com.typesafe.slick" %% "slick" % "3.2.1" % "test",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.1" % "test"
)

// Unfortunately SqlServer tests are flaky when run in parallel
// TODO remove this and re-enable parallel tests
parallelExecution in Test := false

assemblyJarName in assembly := "DBSubsetter.jar"
test in assembly := {}
