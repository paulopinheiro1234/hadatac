organization := "org.hadatac"

name := "hadatac"

version := "10.0.1-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.12.4"
//  "2.13.4"

val playPac4jVersion = "10.0.1"
val pac4jVersion = "4.0.3"
val playVersion = "2.8.2"
val guiceVersion = "4.2.2"

val guiceDeps = Seq(
  "com.google.inject" % "guice" % guiceVersion,
  "com.google.inject.extensions" % "guice-assistedinject" % guiceVersion
)

libraryDependencies ++= Seq(
  "com.feth" %% "play-easymail" % "0.9.0",
  guice,
  caffeine,
  //ehcache,
  //  cacheApi,
  evolutions,
  javaWs,
  javaJdbc,
  "org.webjars" % "bootstrap" % "3.2.0",
  "org.easytesting" % "fest-assert" % "1.4" % "test",
  "org.seleniumhq.selenium" % "selenium-java" % "2.52.0" % "test",
  "org.apache.commons" % "commons-text" % "1.6",
  "commons-validator" % "commons-validator" % "1.5.0",
  "org.apache.solr" % "solr-solrj" % "7.5.0",
  "org.apache.jena" % "jena-core" % "3.0.1",
  "org.apache.jena" % "jena-arq" % "3.0.1",
  "org.eclipse.rdf4j" % "rdf4j-model" % "3.0.0",
  "org.eclipse.rdf4j" % "rdf4j-repository-api" % "3.0.0",
  "org.eclipse.rdf4j" % "rdf4j-runtime" % "3.0.0",
  "args4j" % "args4j" % "2.33",
  "joda-time" % "joda-time" % "2.9.2",
  "org.jasypt" % "jasypt" % "1.9.2",
  "com.typesafe.play" %% "play-iteratees" % "2.6.1",
  "com.typesafe.play" %% "play-iteratees-reactive-streams" % "2.6.1",
  "com.googlecode.json-simple" % "json-simple" % "1.1.1",
  "com.google.code.gson" % "gson" % "2.8.2",
  "org.apache.commons" % "commons-jcs" % "2.2.1" pomOnly(),
  "org.eclipse.paho" % "org.eclipse.paho.client.mqttv3" % "1.0.2",
  "org.pac4j" %% "play-pac4j" % playPac4jVersion,
  "org.pac4j" % "pac4j-http" % pac4jVersion,
  "org.pac4j" % "pac4j-cas" % pac4jVersion,
  "org.pac4j" % "pac4j-openid" % pac4jVersion exclude("xml-apis" , "xml-apis"),
  "org.pac4j" % "pac4j-oauth" % pac4jVersion,
  "org.pac4j" % "pac4j-saml-opensamlv3" % pac4jVersion exclude("org.springframework", "spring-core"),
  "org.pac4j" % "pac4j-oidc" % pac4jVersion exclude("commons-io" , "commons-io"),
  //  "org.pac4j" % "pac4j-gae" % pac4jVersion,
  "org.pac4j" % "pac4j-jwt" % pac4jVersion exclude("commons-io" , "commons-io"),
  "org.pac4j" % "pac4j-ldap" % pac4jVersion,
  "org.pac4j" % "pac4j-sql" % pac4jVersion,
  "org.pac4j" % "pac4j-mongo" % pac4jVersion,
  "org.pac4j" % "pac4j-kerberos" % pac4jVersion exclude("org.springframework", "spring-core"),
  "org.pac4j" % "pac4j-couch" % pac4jVersion,
  "com.typesafe.play" % "play-cache_2.12" % playVersion,
  "commons-io" % "commons-io" % "2.8.0",
  //  "be.objectify" %% "deadbolt-java" % "2.6.1",
  "be.objectify" %% "deadbolt-java" % "2.8.1",
  "com.fasterxml.jackson.module" % "jackson-module-scala_2.12" % "2.11.0",
  "org.apache.poi" % "poi-ooxml" % "3.14",
  "org.apache.commons" % "commons-configuration2" % "2.0",
  "com.typesafe.play" %% "play-mailer" % "8.0.1",
  //  "com.typesafe.play" %% "play-mailer-guice" % "8.0.1",
  "org.springframework.security" % "spring-security-crypto" % "5.4.1",
  "be.objectify" %% "deadbolt-java-gs" % "2.6.0",

  //For Java > 8
  "javax.xml.bind" % "jaxb-api" % "2.3.1",
  "javax.annotation" % "javax.annotation-api" % "1.3.2",
  "javax.el" % "javax.el-api" % "3.0.0",
  "org.glassfish" % "javax.el" % "3.0.0"

) ++ guiceDeps //For Play 2.6 & JDK9

resolvers ++= Seq(Resolver.mavenLocal, "Sonatype snapshots repository" at "https://oss.sonatype.org/content/repositories/snapshots/", "Shibboleth releases" at "https://build.shibboleth.net/nexus/content/repositories/releases/",
  "Spring Framework Security" at "https://mvnrepository.com/artifact/org.springframework.security/spring-security-crypto")

routesGenerator := InjectedRoutesGenerator
