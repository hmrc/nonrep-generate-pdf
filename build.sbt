import com.github.nscala_time.time.Imports.LocalDate

enablePlugins(GitVersioning)
enablePlugins(BuildInfoPlugin)

val akkaHttpVersion = "10.2.1"
val akkaVersion = "2.6.10"
val logbackVersion = "1.2.3"
val metricsVersion = "1.3.0"
val prometheusClientsVersion = "0.9.0"
val circeVersion = "0.13.0"
val ditoSdkVersion = "2.0.0"

val projectName = "generate-pdf"

lazy val createVersionFile = taskKey[Unit]("Create version file")
createVersionFile := {
  import java.nio.file.{Paths, Files}
  import java.nio.charset.StandardCharsets
  Files.write(Paths.get("version.txt"), version.value.getBytes(StandardCharsets.UTF_8))
}

lazy val IntegrationTest = config("it") extend(Test)

val headerSettings = Seq(
  headerLicense := Some(HeaderLicense.ALv2(LocalDate.now().getYear.toString, "HM Revenue & Customs"))
)

lazy val root = (project in file(".")).
  configs(IntegrationTest).
  enablePlugins(AutomateHeaderPlugin).
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
    headerSettings,
    resolvers ++= Seq(
      "itext-dito" at "https://repo.itextsupport.com/dito",
      "itext-releases" at "https://repo.itextsupport.com/releases",
      "nexus" at "https://repository.mulesoft.org/nexus/content/repositories/public"
    ),

    libraryDependencies ++= Seq(
      "com.lightbend.akka"     %% "akka-stream-alpakka-s3"       % "2.0.2",
      "com.typesafe.akka"      %% "akka-http"                    % akkaHttpVersion,
      "com.typesafe.akka"      %% "akka-http-xml"                % akkaHttpVersion,
      "com.typesafe.akka"      %% "akka-actor-typed"             % akkaVersion,
      "com.typesafe.akka"      %% "akka-stream"                  % akkaVersion,
      "ch.qos.logback"         %  "logback-classic"              % logbackVersion,
      "ch.qos.logback"         %  "logback-core"                 % logbackVersion,
      "com.typesafe.akka"      %% "akka-slf4j"                   % akkaVersion,
      "org.slf4j"              %  "slf4j-api"                    % "1.7.30",
      "net.logstash.logback"   %  "logstash-logback-encoder"     % "6.1",
      "fr.davit"               %% "akka-http-metrics-prometheus" % metricsVersion,
      "io.prometheus"          %  "simpleclient_common"          % prometheusClientsVersion,
      "io.prometheus"          %  "simpleclient_dropwizard"      % prometheusClientsVersion,
      "io.prometheus"          %  "simpleclient_hotspot"         % prometheusClientsVersion,

      "io.circe" %% "circe-core"        % circeVersion,
      "io.circe" %% "circe-generic"     % circeVersion,
      "io.circe" %% "circe-parser"      % circeVersion,
      "io.circe" %% "circe-optics"      % circeVersion,
      "io.circe" %% "circe-literal"     % circeVersion,
      "io.circe" %% "circe-json-schema" % "0.1.0",

      "com.typesafe.akka"    %% "akka-testkit"             % akkaVersion     % Test,
      "com.typesafe.akka"    %% "akka-http-testkit"        % akkaHttpVersion % Test,
      "com.typesafe.akka"    %% "akka-actor-testkit-typed" % akkaVersion     % Test,
      "com.typesafe.akka"    %% "akka-stream-testkit"      % akkaVersion     % Test,
      "org.scalatest"        %% "scalatest"                % "3.2.2"         % Test,
      "org.scalamock"        %% "scalamock"                % "4.3.0"         % Test,

      "com.itextpdf.dito" % "sdk-java" % ditoSdkVersion,

    ),

    mainClass in assembly := Some("uk.gov.hmrc.nonrep.pdfs.server.Main"),
    assemblyJarName in assembly := s"$projectName.jar",
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
      case PathList("META-INF", "BCKEY.DSA") => MergeStrategy.discard
      case PathList("META-INF", "BC1024KE.DSA") => MergeStrategy.discard
      case PathList("META-INF", "BC2048KE.DSA") => MergeStrategy.discard
      case "reference.conf" => MergeStrategy.concat
      case _ => MergeStrategy.first
    }

  )



scalacOptions ++= Seq("-deprecation", "-feature")
testOptions in Test += Tests.Argument("-oF")
fork in Test := true
envVars in Test := Map("WORKING_DIR" -> "/tmp/unit-tests")

organizationName := "HM Revenue & Customs"
startYear := Some(2021)
licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0"))
