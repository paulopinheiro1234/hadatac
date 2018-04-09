organization := "org.hadatac"

name := "hadatac"

scalaVersion := "2.11.8"

version := "1.0-SNAPSHOT"

val appDependencies = Seq(
  "be.objectify" %% "deadbolt-java" % "2.5.0",
  "com.feth" %% "play-authenticate" % "0.8.3",
  cache,
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
  "org.apache.commons" % "commons-text" % "1.1",
  "commons-validator" % "commons-validator" % "1.5.0",
  //"org.apache.httpcomponents" % "httpclient" % "4.5.2",
  //"org.apache.httpcomponents" % "fluent-hc" % "4.5.2",
  "org.apache.poi" % "poi-ooxml" % "3.14",
  "org.apache.solr" % "solr-solrj" % "6.5.0",
  "org.apache.jena" % "jena-core" % "3.0.1",
  "org.apache.jena" % "jena-arq" % "3.0.1",
  "args4j" % "args4j" % "2.33",
  "joda-time" % "joda-time" % "2.9.2",
  "org.jasypt" % "jasypt" % "1.9.2",
  "org.labkey" % "labkey-client-api" % "16.2.0",
  "com.googlecode.json-simple" % "json-simple" % "1.1.1",
  "com.google.code.gson" % "gson" % "2.8.2"
)

// add resolver for deadbolt and easymail snapshots
resolvers += Resolver.sonatypeRepo("snapshots")

javacOptions ++= Seq("-Xlint:deprecation")
javacOptions ++= Seq("-Xlint:unchecked")

lazy val root = project.in(file("."))
  .enablePlugins(PlayJava, PlayEbean)
  .enablePlugins(GitVersioning)
  .settings(
    libraryDependencies ++= appDependencies
  )

//libraryDependencies += guice
//libraryDependencies += openId

//dependencyOverrides += "com.typesafe.play" % "twirl-api_2.11" % "1.3.2"
//dependencyOverrides += "com.typesafe.play" % "play-logback_2.11" % "2.6.0"
//dependencyOverrides += "com.typesafe.play" % "play-server_2.11" % "2.6.0"

useJGit

