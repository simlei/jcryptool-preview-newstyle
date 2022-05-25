import sbt._
import sbtassembly.MergeStrategy
import libjctbuild.JCTBuild._
import sbtproj.JCTApi._
import sbtproj.JCT
import sbtproj.JCT.Keys._
import sbtproj.BCIAssembly
import os.Path


resolvers += Resolver.sonatypeRepo("snapshots")

(ThisBuild / scalaVersion) := sbtproj.all.scala3Version

/* javaOptions ++= Seq( */
/* "-XX:-OmitStackTraceInFastThrow" */
/*   ) */

/* ThisBuild / trackInternalDependencies := TrackLevel.TrackIfMissing */
/* ThisBuild / exportJars := true */

// TODO: introduce jctbuild libraries
lazy val root = project
  .in(file("."))
  .settings(
    name := "bci_logic",
    version := "0.1.0-SNAPSHOT",
    libraryDependencies ++= Dependencies.zios,
    libraryDependencies ++= Dependencies.cats,
    libraryDependencies ++= Dependencies.bouncycastles,
    libraryDependencies ++= Dependencies.oslibs,
    libraryDependencies ++= Dependencies.flexmarks,
  )
  .settings(sbtproj.all.commonSettings)
  .settings(sbtproj.JCT.defaultSettings)
  .settings(inConfig(RCPExport)(BCIAssembly.rcpExportAssemblySettings))
  .settings(BCIAssembly.baseAssemblySettings)
  .dependsOn(graphs)

lazy val arturopala_tree = (project in file("sub/arturopala_tree"))
  .settings(sbtproj.all.commonSettings)
lazy val graphs = (project in file("sub/graphs"))
  .settings(sbtproj.all.commonSettings)
  /* .dependsOn(arturopala_tree) */




assembly / mainClass := Some("tsec.bci.CLIAppOps")

jct_pde_gitbase   := Path("/home/snuc/git/JCT_bciws")
jct_pde_workspace := Path("/home/snuc/git/JCT_bciws/bcidevWS")
jct_pde_datadir   := Path("/home/snuc/git/JCT_bciws/runtime-jcryptool.product")
jct_pde_runconfig := "JCT_other_runconfig"
jct_pde_exportjar_path := file(jct_pde_gitbase.value.toString + "/core/org.jcryptool.bci/bcijar/bciassembled.jar")

val toRCPAndRunHelp = taskKey[Unit]("copy assembled jar to RCP and run it, navigating directly to the help page")
toRCPAndRunHelp := {
  val toRCPDone = toRCP.value
  pdedevApi.value.runcmd(
    jct_pde_runconfig.value,
    jct_pde_datadir.value
  ).plus("-StartupHelp", "/org.jcryptool.bci/$nl$/help/content/index.html")
  .connectOut.printdbg.call()
}



Compile / unmanagedJars := jctjars_swtplus_list.value ++ jctjars_jctplatform_list.value
fork := true

// enablePlugins(SbtOsgi)

/* testtask := {println("Overwritten!")} */
/* lazy val jctTestTask = taskKey[Unit]("jctTestTask") */
/* jctTestTask := pdedev.testtask.asTaskImpl */
