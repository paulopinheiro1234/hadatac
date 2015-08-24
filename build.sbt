organization := "org.hadatac"

name := "hadatac"

scalaVersion := "2.11.6"

version := "1.0-SNAPSHOT"

val appDependencies = Seq(
  "be.objectify"  %% "deadbolt-java"     % "2.4.0",
  "com.feth"      %% "play-authenticate" % "0.7.0-SNAPSHOT",
  "org.postgresql"    %  "postgresql"        % "9.4-1201-jdbc41",
  cache,
  javaWs,
  javaJdbc,
  "org.webjars" % "bootstrap" % "3.2.0",
  "org.easytesting" % "fest-assert" % "1.4" % "test",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.5.1",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.5.1",
  "com.fasterxml.jackson.core" % "jackson-annotations" % "2.5.1",
  "org.apache.commons" % "commons-io" % "1.3.2",
  "org.apache.commons" % "commons-csv" % "1.1",
  "org.apache.httpcomponents" % "httpclient" % "4.3.6",
  "org.apache.httpcomponents" % "fluent-hc" % "4.3.6",
  "org.apache.poi" % "poi-ooxml" % "3.9",
  "org.apache.solr" % "solr-solrj" % "5.2.1",
  "org.apache.jena" % "jena-core" % "2.13.0",
  "org.apache.jena" % "jena-arq" % "2.13.0",
  "args4j" % "args4j" % "2.32",
  "joda-time" % "joda-time" % "2.8.2"
)

// add resolver for deadbolt and easymail snapshots
resolvers += Resolver.sonatypeRepo("snapshots")

lazy val root = project.in(file("."))
  .enablePlugins(PlayJava, PlayEbean)
  .settings(
    libraryDependencies ++= appDependencies
  )

