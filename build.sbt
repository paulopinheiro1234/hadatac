organization := "org.hadatac"

name := "hadatac"

scalaVersion := "2.11.12"

version := "1.0-SNAPSHOT"

val appDependencies = Seq(
  "be.objectify" %% "deadbolt-java" % "2.6.4",
  "be.objectify" %% "deadbolt-java-gs" % "2.6.0",
  "com.feth" %% "play-authenticate" % "0.9.0",
  cacheApi,
  ehcache,
  evolutions,
  javaWs,
  javaJdbc,
  "org.webjars" % "bootstrap" % "3.2.0",
  "org.easytesting" % "fest-assert" % "1.4" % "test",
  "org.seleniumhq.selenium" % "selenium-java" % "2.52.0" % "test",
  //"com.fasterxml.jackson.core" % "jackson-core" % "2.7.2",
  //"com.fasterxml.jackson.core" % "jackson-databind" % "2.7.2",
  //"com.fasterxml.jackson.core" % "jackson-annotations" % "2.7.2",
  //"commons-io" % "commons-io" % "2.4",
  //"org.apache.commons" % "commons-csv" % "1.2",
  "org.apache.commons" % "commons-text" % "1.6",
  "commons-validator" % "commons-validator" % "1.5.0",
  //"org.apache.httpcomponents" % "httpclient" % "4.5.2",
  //"org.apache.httpcomponents" % "fluent-hc" % "4.5.2",
  "org.apache.poi" % "poi-ooxml" % "3.14",
  "org.apache.commons" % "commons-configuration2" % "2.0",
  "org.apache.solr" % "solr-solrj" % "7.5.0",
  "org.apache.jena" % "jena-core" % "3.0.1",
  "org.apache.jena" % "jena-arq" % "3.0.1",
  "org.eclipse.rdf4j" % "rdf4j-model" % "3.0.0",
  "org.eclipse.rdf4j" % "rdf4j-repository-api" % "3.0.0",
  "org.eclipse.rdf4j" % "rdf4j-runtime" % "3.0.0",
  //"de.fuberlin.wiwiss.ng4j" % "ng4j" % "0.9.3" from "file:///public/lib/ng4j-0.9.3/lib/ng4j-0.9.3.jar",
  "args4j" % "args4j" % "2.33",
  "joda-time" % "joda-time" % "2.9.2",
  "org.jasypt" % "jasypt" % "1.9.2",
  //"org.labkey" % "labkey-client-api" % "16.2.0",
  "com.typesafe.play" %% "play-json" % "2.6.0",
  "com.typesafe.play" %% "play-iteratees" % "2.6.1",
  "com.typesafe.play" %% "play-iteratees-reactive-streams" % "2.6.1",
  "com.googlecode.json-simple" % "json-simple" % "1.1.1",
  "com.google.code.gson" % "gson" % "2.8.2",
  "org.apache.commons" % "commons-jcs" % "2.2.1" pomOnly(), 
  "org.eclipse.paho" % "org.eclipse.paho.client.mqttv3" % "1.0.2"
)

// add resolver for deadbolt and easymail snapshots
resolvers += Resolver.sonatypeRepo("snapshots")

javacOptions ++= Seq("-Xlint:deprecation")
javacOptions ++= Seq("-Xlint:unchecked")
javaOptions ++= Seq("-Dorg.eclipse.rdf4j.rio.verify_uri_syntax=false")

lazy val root = project.in(file("."))
  .enablePlugins(PlayJava, PlayEbean)
  .settings(
    libraryDependencies ++= appDependencies
  )

libraryDependencies += guice
libraryDependencies += openId


