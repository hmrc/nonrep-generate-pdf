enablePlugins(GitVersioning)
enablePlugins(BuildInfoPlugin)

val akkaHttpVersion = "10.2.0"
val akkaVersion = "2.6.9"
val logbackVersion = "1.2.3"
val metricsVersion = "4.1.0"
val circeVersion = "0.13.0"

val projectName = "generate-pdf"

lazy val createVersionFile = taskKey[Unit]("Create version file")
createVersionFile := {
  import java.nio.file.{Paths, Files}
  import java.nio.charset.StandardCharsets
  Files.write(Paths.get("version.txt"), version.value.getBytes(StandardCharsets.UTF_8))
}

lazy val IntegrationTest = config("it") extend(Test)

lazy val root = (project in file(".")).
  configs(IntegrationTest).
  settings(
    Defaults.itSettings,
    inThisBuild(List(
      organization := "uk.gov",
      git.useGitDescribe := true,
      scalaVersion := "2.13.3"
    )),
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "uk.gov.hmrc.nonrep",
    name := projectName,

    resolvers ++= Seq(
      Resolver.bintrayRepo("lonelyplanet", "maven"),
      Resolver.bintrayRepo("hmrc", "releases")
    ),

    libraryDependencies ++= Seq(
      "com.typesafe.akka"    %% "akka-http"                % akkaHttpVersion,
      "com.typesafe.akka"    %% "akka-actor-typed"         % akkaVersion,
      "com.typesafe.akka"    %% "akka-stream"              % akkaVersion,
      "ch.qos.logback"       % "logback-classic"           % logbackVersion,
      "ch.qos.logback"       % "logback-core"              % logbackVersion,
      "com.typesafe.akka"    %% "akka-slf4j"               % akkaVersion,
      "org.slf4j"            % "slf4j-api"                 % "1.7.30",
      "net.logstash.logback" % "logstash-logback-encoder"  % "6.1",

      "io.circe" %% "circe-core"    % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser"  % circeVersion,
      "io.circe" %% "circe-optics"  % circeVersion,

      "com.typesafe.akka"    %% "akka-http-testkit"        % akkaHttpVersion % Test,
      "com.typesafe.akka"    %% "akka-actor-testkit-typed" % akkaVersion     % Test,
      "com.typesafe.akka"    %% "akka-stream-testkit"      % akkaVersion     % Test,
      "org.scalatest"        %% "scalatest"                % "3.2.2"         % Test,
      "org.scalamock"        %% "scalamock"                % "4.3.0"         % Test,

    ),

    assemblyJarName in assembly := s"$projectName.jar",
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
      case PathList("META-INF", "BCKEY.DSA") => MergeStrategy.discard
      case "reference.conf" => MergeStrategy.concat
      case _ => MergeStrategy.first
    }

  )

scalacOptions ++= Seq("-deprecation", "-feature")
testOptions in Test += Tests.Argument("-oF")
fork in Test := true
envVars in Test := Map("WORKING_DIR" -> "/tmp/unit-tests")