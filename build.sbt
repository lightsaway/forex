import Dependencies._

name := "forex"
version := "1.0.1"

scalaVersion := "2.12.10"
scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:experimental.macros",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xfuture",
  "-Xlint",
  "-Ydelambdafy:method",
  "-Xlog-reflective-calls",
  "-Yno-adapted-args",
  "-Ypartial-unification",
  "-Ywarn-dead-code",
  "-Ywarn-inaccessible",
  "-Ywarn-unused-import",
  "-Ywarn-value-discard"
)

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  compilerPlugin(Libraries.kindProjector),
  Libraries.cats,
  Libraries.catsEffect,
  Libraries.http4sDsl,
  Libraries.http4sServer,
  Libraries.http4sClient,
  Libraries.http4sCirce,
  Libraries.circeCore,
  Libraries.circeGeneric,
  Libraries.circeGenericExt,
  Libraries.circeParser,
  Libraries.circeJava8,
  Libraries.pureConfig,
  Libraries.pureConfigHttp4s,
  Libraries.enumeratum,
  Libraries.logback,
  Libraries.circeLiteral   % Test,
  Libraries.scalaTest      % Test,
  Libraries.scalaCheck     % Test,
  Libraries.catsScalaCheck % Test
)

mainClass in Compile := Some("forex.Main")

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
version in Docker := "latest"
dockerExposedUdpPorts += 8080
