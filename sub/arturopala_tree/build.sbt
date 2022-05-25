import sbtproj.JCT.Keys._
name := "arturopala-tree"
scalaVersion := sbtproj.all.scala3Version

libraryDependencies += "com.github.arturopala" %% "buffer-and-slice" % "1.55.0"
trackInternalDependencies := TrackLevel.TrackIfMissing
