name := "spark-google-spreadsheets"

organization := "com.github.potix2"

scalaVersion := "2.12.11"

crossScalaVersions := Seq("2.11.11", "2.12.11")

val buildVersion = sys.env.getOrElse("GO_PIPELINE_LABEL", "1.0-SNAPSHOT")
version := s"$buildVersion-indix"

spName := "potix2/spark-google-spreadsheets"

spAppendScalaVersion := true

spIncludeMaven := true

spIgnoreProvided := true

sparkVersion := "2.4.5"

val testSparkVersion = settingKey[String]("The version of Spark to test against.")

testSparkVersion := sys.props.get("spark.testVersion").getOrElse(sparkVersion.value)

sparkComponents := Seq("sql")

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.5" % "provided",
  "org.scalatest" %% "scalatest" % "3.1.2" % "test",
  ("com.google.api-client" % "google-api-client" % "1.22.0").
    exclude("com.google.guava", "guava-jdk5"),
  ("com.google.oauth-client" % "google-oauth-client-jetty" % "1.22.0").
    exclude("org.mortbay.jetty", "servlet-api"),
  "com.google.apis" % "google-api-services-sheets" % "v4-rev18-1.22.0",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.11.127",
  "io.spray" %% "spray-json" % "1.3.2"
)

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % testSparkVersion.value % "test" force(),
  "org.apache.spark" %% "spark-sql" % testSparkVersion.value % "test"  force(),
  "org.scala-lang" % "scala-library" % scalaVersion.value % "compile",
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % "compile"
)

/**
 * release settings
 */
publishMavenStyle := true

releaseCrossBuild := true

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

releasePublishArtifactsAction := PgpKeys.publishSigned.value

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

publishTo := {
  if (version.value.endsWith("SNAPSHOT"))
    Some("Indix Snapshot Artifactory" at "http://artifacts.indix.tv:8081/artifactory/libs-snapshot-local")
  else
    Some("Indix Release Artifactory"  at "http://artifacts.indix.tv:8081/artifactory/libs-release-local")
}

pomExtra := (
  <url>https://github.com/potix2/spark-google-spreadsheets</url>
  <scm>
    <url>git@github.com:potix2/spark-google-spreadsheets.git</url>
    <connection>scm:git:git@github.com:potix2/spark-google-spreadsheets.git</connection>
  </scm>
  <developers>
    <developer>
      <id>potix2</id>
      <name>Katsunori Kanda</name>
      <url>https://github.com/potix2/</url>
    </developer>
  </developers>)

// Skip tests during assembly
test in assembly := {}

import ReleaseTransformations._

// Add publishing to spark packages as another step.
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  pushChanges,
  releaseStepTask(spPublish)
)
