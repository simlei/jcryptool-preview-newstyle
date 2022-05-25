addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.15.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-osgi" % "0.9.6")

// only another trial candidate: lazy val jctbuild_localproj = ProjectRef(file("/home/snuc/sandbox/bct/jctbuild"), "exported_local_project")

// // for local dependency
// lazy val jctbuild_localproj = RootProject(file("/home/snuc/sandbox/bct/jctbuild"))
// lazy val root = (project in file("."))
//                 .dependsOn(jctbuild_localproj)


lazy val jctbuild_localrepo = "org.jcryptool" %% "jctbuild" % "0.1-SNAPSHOT"
libraryDependencies += jctbuild_localrepo
/* addSbtPlugin("org.jcryptool" % "jctbuild" % "0.1-SNAPSHOT") */




