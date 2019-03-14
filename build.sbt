import scala.util.Try
import xerial.sbt.Sonatype._

name := "sews-bridge"

organization := "im.mange"

version := Try(sys.env("TRAVIS_BUILD_NUMBER")).map("0.0." + _).getOrElse("1.0-SNAPSHOT")

scalaVersion := "2.12.4"

resolvers ++= Seq(
  "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/",
  "Tim Tennant's repo" at "http://dl.bintray.com/timt/repo/"
)

unmanagedSourceDirectories in Test += baseDirectory.value / "src" / "example" / "scala"

libraryDependencies ++= Seq(
  "io.shaka" %% "naive-http" % "94",
  "org.eclipse.jetty.websocket" % "websocket-server" % "9.2.24.v20180105", // % "provided",
  //9.4.8.v20171121 - see http://central.maven.org/maven2/org/eclipse/jetty/jetty-distribution/

  "io.argonaut" %% "argonaut" % "6.2.2",
  "com.davegurnell" %% "bridges" % "0.12.1",

  //  "com.github.alexarchambault" %% "argonaut-shapeless_6.2" % "1.2.0-M4",
  //  "org.reactormonk" % "elmtypes_2.12" % "0.4",

  //60
  "im.mange" %% "little" % "[0.0.49,0.0.999]" % "provided"
)

sonatypeSettings

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

homepage := Some(url("https://github.com/alltonp/sews-bridge"))

licenses +=("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))

credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", System.getenv("SONATYPE_USER"), System.getenv("SONATYPE_PASSWORD"))

pomExtra :=
    <scm>
      <url>git@github.com:alltonp/sews-bridge.git</url>
      <connection>scm:git:git@github.com:alltonp/sews-bridge.git</connection>
    </scm>
    <developers>
      <developer>
        <id>alltonp</id>
      </developer>
    </developers>
