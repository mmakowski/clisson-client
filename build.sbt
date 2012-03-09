organization := "com.bimbr"

name := "clisson-client"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.9.1"

crossPaths := false

libraryDependencies ++= Seq(
  "com.bimbr"                 % "clisson-protocol" % "0.1.0-SNAPSHOT",
  "org.apache.httpcomponents" % "httpclient"       % "4.1.3",
  "org.slf4j"                 % "slf4j-api"        % "1.6.4",
  "junit"                     % "junit"            % "4.10"            % "test", 
  "org.mockito"               % "mockito-all"      % "1.9.0"           % "test",
  "org.mortbay.jetty"         % "jetty"            % "6.1.25"          % "test",
  "org.specs2"               %% "specs2"           % "1.8.2"           % "test"  
)
