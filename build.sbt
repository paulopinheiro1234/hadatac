organization := "org.hadatac"

name := "hadatac"

scalaVersion := "2.11.7"

version := "1.0-SNAPSHOT"

val appDependencies = Seq(
  ws,
  "be.objectify" %% "deadbolt-java-gs" % "2.6.0",
  "com.feth" % "play-authenticate_2.11" % "0.8.3",
  "org.webjars" % "bootstrap" % "4.0.0-beta",
  "org.easytesting" % "fest-assert" % "1.4" % "test",
  "org.apache.commons" % "commons-csv" % "1.5",
  "org.apache.commons" % "commons-text" % "1.2",
  "commons-validator" % "commons-validator" % "1.6",
  "org.apache.httpcomponents" % "httpclient" % "4.5.4",
  "org.apache.httpcomponents" % "fluent-hc" % "4.5.4",
  "org.apache.poi" % "poi-ooxml" % "3.17",
  "org.apache.solr" % "solr-solrj" % "6.5.0",
  "org.apache.jena" % "jena-core" % "3.6.0",
  "org.apache.jena" % "jena-arq" % "3.6.0",
  "args4j" % "args4j" % "2.33",
  "joda-time" % "joda-time" % "2.9.9",
  "org.jasypt" % "jasypt" % "1.9.2",
  "org.labkey" % "labkey-client-api" % "16.2.0",
  "com.googlecode.json-simple" % "json-simple" % "1.1.1",
  "com.google.code.gson" % "gson" % "2.8.2",
  "com.google.inject" % "guice" % "4.0",
  "javax.servlet" % "javax.servlet-api" % "4.0.0" % "provided"
)

evictionWarningOptions in update := EvictionWarningOptions.default.withWarnTransitiveEvictions(false)

// add resolver for deadbolt and easymail snapshots
resolvers += Resolver.sonatypeRepo("snapshots")

lazy val root = project.in(file("."))
  .enablePlugins(PlayJava)
  .settings(
    libraryDependencies ++= appDependencies
  )

libraryDependencies += guice
libraryDependencies += openId

//dependencyOverrides += "com.typesafe.play" % "twirl-api_2.11" % "1.3.2"
//dependencyOverrides += "com.typesafe.play" % "play-logback_2.11" % "2.6.0"
//dependencyOverrides += "com.typesafe.play" % "play-server_2.11" % "2.6.0"

fork in run := true
