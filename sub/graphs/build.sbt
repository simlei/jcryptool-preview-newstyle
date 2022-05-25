import sbtproj.JCT.Keys._
name := "graphs"

libraryDependencies += "com.softwaremill.magnolia1_3" %% "magnolia" % "1.1.0"
libraryDependencies += "com.github.arturopala" %% "buffer-and-slice" % "1.55.0"
libraryDependencies += "hu.webarticum" % "tree-printer" % "2.0.0"
libraryDependencies ++= Dependencies.cats
libraryDependencies ++= Dependencies.zios
