name := """play-cloudwickone-mongo-template"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  cache,
  ws,
  filters,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.9",
  "org.reactivemongo" %% "reactivemongo-extensions-json" % "0.11.7.play24",
  "com.etaty.rediscala" %% "rediscala" % "1.4.2",
  "com.mohiva" %% "play-silhouette" % "3.0.0",
  "net.ceedubs" %% "ficus" % "1.1.2",
  // web jars
  "org.webjars" % "bootstrap" % "3.3.4",
  "org.webjars" %% "webjars-play" % "2.4.0-2",
  "com.adrianhurt" %% "play-bootstrap" % "1.0-P24-B3-SNAPSHOT" exclude("org.webjars", "jquery"),
  "org.webjars" % "jquery" % "2.2.0",
  "org.webjars" % "font-awesome" % "4.5.0",
  "org.webjars" % "bootstrap-datepicker" % "1.4.0",
  specs2 % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
