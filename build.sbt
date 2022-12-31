scalaVersion := "2.12.16"

name := "ergo-names-plasma"
organization := "io.ergonames"
version := "0.0.2"

libraryDependencies ++= Seq(
    "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1",
    "io.github.getblok-io" % "getblok_plasma_2.12" % "1.0.0",
    "org.scalaj" %% "scalaj-http" % "2.4.2",
    "org.rocksdb" % "rocksdbjni" % "7.8.3"
)

dependencyOverrides += "org.ergoplatform" %% "ergo-appkit" % "ac116c85-SNAPSHOT"

resolvers ++= Seq(
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "SonaType" at "https://oss.sonatype.org/content/groups/public",
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Bintray" at "https://jcenter.bintray.com/"
)