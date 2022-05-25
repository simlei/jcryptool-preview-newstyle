package sbtproj
import sbt._
import sbt.Keys._

object all {
  // val scala3Version = "3.2.0-RC1-bin-20220511-7c446ce-NIGHTLY"
  // val scala3Version = "3.1.2"
  val scala3Version = "3.1.3-RC2"
  // val scala3Version = "3.2.0-RC1-bin-20220521-0181ef9-NIGHTLY"

  val commonSettings = Seq(
      scalaVersion := scala3Version,
      scalacOptions ++= Seq(
        "-explain",
        // "-Ywarn-value-discard", // not yet implemented...
        // "-Xfatal-warnings" // annoying...
      )
    )
}
