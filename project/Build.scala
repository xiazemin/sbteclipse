import sbt._
import sbt.Keys._
import sbt.ScriptedPlugin._
import sbtrelease.ReleasePlugin._
import com.typesafe.sbt.SbtScalariform._
import bintray.Plugin.bintrayPublishSettings
import bintray.Keys._
import com.typesafe.sbt.SbtGit._

object Build extends Build {

  val baseVersion = "4.0.0"

  lazy val root = Project(
    "sbteclipse",
    file("."),
    aggregate = Seq(sbteclipseCore, sbteclipsePlugin),
    settings = commonSettings ++ Seq(
      publishArtifact := false
    )
  )

  lazy val sbteclipseCore = Project(
    "sbteclipse-core",
    file("sbteclipse-core"),
    settings = commonSettings ++ Seq(
      libraryDependencies ++= Seq(
       "org.scalaz" %% "scalaz-core"   % "7.1.0",
       "org.scalaz" %% "scalaz-effect" % "7.1.0")
    )
  )

  lazy val sbteclipsePlugin = Project(
    "sbteclipse-plugin",
    file("sbteclipse-plugin"),
    dependencies = Seq(sbteclipseCore),
    settings = commonSettings ++ Seq(
      publishLocal := {
        val depOn = (publishLocal in sbteclipseCore).value
        publishLocal.value
      }
    )
  )

  def commonSettings =
    versionWithGit ++
    scalariformSettings ++
    scriptedSettings ++
    releaseSettings ++
    bintrayPublishSettings ++
    Seq(
      git.baseVersion := baseVersion,
      organization := "com.typesafe.sbteclipse",
      // version is defined in version.sbt in order to support sbt-release
      scalacOptions ++= Seq("-unchecked", "-deprecation"),
      sbtPlugin := true,
      publishMavenStyle := false,
      sbtVersion in GlobalScope := {
        System.getProperty("sbt.build.version", (sbtVersion in GlobalScope).value)
      },
      scalaVersion := {
        (sbtVersion in GlobalScope).value match {
          case sbt013 if sbt013.startsWith("0.13.") => "2.10.5"
          case sbt012 if sbt012.startsWith("0.12.") => "2.9.3"
          case _ => "2.9.3"
        }
      },
      sbtDependency in GlobalScope := {
        (sbtDependency in GlobalScope).value.copy(revision = (sbtVersion in GlobalScope).value)
      },
      publishMavenStyle := false,
      bintrayOrganization in bintray := None,
      repository in bintray := "sbt-plugins",
      publishArtifact in (Compile, packageDoc) := false,
      publishArtifact in (Compile, packageSrc) := false,
      // Uncomment the following line to get verbose output
      scriptedBufferLog := false,
      scriptedLaunchOpts ++= List("-Dplugin.version=" + version.value)
    )
}
