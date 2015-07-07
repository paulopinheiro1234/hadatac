name := """hadatac-browser"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "com.fasterxml.jackson.core" % "jackson-core" % "2.5.1",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.5.1",
  "com.fasterxml.jackson.core" % "jackson-annotations" % "2.5.1",
  "org.apache.commons" % "commons-io" % "1.3.2",
  "org.apache.httpcomponents" % "httpclient" % "4.3.6",
  "org.apache.httpcomponents" % "fluent-hc" % "4.3.6"
)


fork in run := true