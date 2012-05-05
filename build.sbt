organization := "com.bimbr"

name := "clisson-client"

version := "0.3.1-SNAPSHOT"

scalaVersion := "2.9.1"

licenses := Seq("BSD-style" -> url("http://www.opensource.org/licenses/bsd-license.php"))

homepage := Some(url("https://github.com/mmakowski/clisson-client"))

libraryDependencies ++= Seq(
  "com.bimbr"                 % "clisson-protocol" % "0.1.0",
  "log4j"                     % "log4j"            % "1.2.16",
  "org.apache.httpcomponents" % "httpclient"       % "4.1.3",
  "org.slf4j"                 % "slf4j-api"        % "1.6.4",
  "junit"                     % "junit"            % "4.10"            % "test", 
  "org.mockito"               % "mockito-all"      % "1.9.0"           % "test",
  "org.mortbay.jetty"         % "jetty"            % "6.1.25"          % "test",
  "org.specs2"               %% "specs2"           % "1.8.2"           % "test"  
)

crossPaths := false

useGpg := true

publishMavenStyle := true

publishArtifact in Test := false

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) 
    Some("snapshots" at nexus + "content/repositories/snapshots") 
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomIncludeRepository := { _ => false }

autoScalaLibrary := false

pomExtra := (
  <scm>
    <url>git://github.com/mmakowski/clisson-client.git</url>
    <connection>scm:git:git://github.com/mmakowski/clisson-client.git</connection>
  </scm>
  <developers>
    <developer>
      <id>mmakowski</id>
      <name>Maciek Makowski</name>
      <url>http://mmakowski.com</url>
    </developer>
  </developers>)