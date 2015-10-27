organization := "org.hadatac"

name := "hadatac"

scalaVersion := "2.11.7"

version := "1.0-SNAPSHOT"

val appDependencies = Seq(
  "be.objectify"  %% "deadbolt-java"     % "2.4.3",
  "com.feth"      %% "play-authenticate" % "0.7.0-SNAPSHOT",
  cache,
  javaWs,
  javaJdbc,
  "org.webjars" % "bootstrap" % "3.3.5",
  "org.easytesting" % "fest-assert" % "1.4" % "test",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.6.3",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.3",
  "com.fasterxml.jackson.core" % "jackson-annotations" % "2.6.3",
  "commons-io" % "commons-io" % "2.4",
  "org.apache.commons" % "commons-csv" % "1.2",
  "commons-validator" % "commons-validator" % "1.4.1",
  "org.apache.httpcomponents" % "httpclient" % "4.5.1",
  "org.apache.httpcomponents" % "fluent-hc" % "4.5.1",
  "org.apache.poi" % "poi-ooxml" % "3.13",
  "org.apache.solr" % "solr-solrj" % "5.3.1",
  "org.apache.jena" % "jena-core" % "3.0.0",
  "org.apache.jena" % "jena-arq" % "3.0.0",
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



fork in run := true
