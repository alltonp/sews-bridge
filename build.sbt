import scala.util.Try
import xerial.sbt.Sonatype._

name := "sews-bridge"

organization := "im.mange"

version := Try(sys.env("TRAVIS_BUILD_NUMBER")).map("0.0." + _).getOrElse("1.0-SNAPSHOT")

scalaVersion:= "2.12.4"

//crossScalaVersions := Seq("2.10.4"/*, "2.11.0"*/)

resolvers ++= Seq(
  "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/"
)

libraryDependencies ++= Seq(
//	"junit" % "junit" % "4.11" % "test->default",
//	"org.scalatest" %% "scalatest" % "2.2.0" % "test"
)

//libraryDependencies := {
//  CrossVersion.partialVersion(scalaVersion.value) match {
//    case Some((2, scalaMajor)) if scalaMajor >= 11 => libraryDependencies.value :+ "org.scala-lang.modules" %% "scala-xml" % "1.0.1"
//    case _ => libraryDependencies.value
//  }
//}

//net.virtualvoid.sbt.graph.Plugin.graphSettings

//TIP: for app publishing ... start commenting from here
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
//TIP: for app publishing ... finish commenting here

homepage := Some(url("https://github.com/alltonp/sews-bridge"))

licenses +=("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))

//TIP: for lib publishing ... comment this too
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
