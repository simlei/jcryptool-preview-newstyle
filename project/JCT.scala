package sbtproj


import os.Path

import cats.effect._
import cats.effect.unsafe.implicits.global

import sbt.{IO => sbtIO, _}
import sbt.Keys._

import libjctbuild.JCTBuild._
import libjctbuild.SimleiGit._
import libjctbuild.Util._
import libjctbuild.SimleiGithub

import scala.collection.mutable.ArrayBuffer

object JCT {
  import JCTApi._

  object Keys {
    val RCPExport = config("RCPExport") extend(Compile)

    val toRCP = taskKey[Unit]("ToRCP")
    val toRCPAndRun = taskKey[Unit]("copy assembled jar to RCP and run it")
    val rcpRun = taskKey[Unit]("run the configured eclipse run config")
    val rcpClearData = taskKey[Unit]("clear the workspace")

    val jctjars_swtplus_list = taskKey[Seq[sbt.Attributed[java.io.File]]]("list_swtplus_jars")
    val jctjars_jctplatform_list = taskKey[Seq[sbt.Attributed[java.io.File]]]("jctjars_swtplus_list")
    val jctjars_swtplus_loc = settingKey[File]("where swtplus jars go")
    val jctjars_jctplatform_loc = settingKey[File]("where jctplatform jars go")

    val jct_pde_exportjar_path = settingKey[File]("the path where the assembled jar for RCPs go")
    val jct_pde_runconfig = settingKey[String]("the runconfig name")
    val jct_pde_datadir = settingKey[Path]("the datadir for PDE launch")
    val jct_pde_gitbase = settingKey[Path]("where the Eclipse PDE development has their JCrypTool core and crypto repos")
    val jct_pde_workspace = settingKey[Path]("where is the workspace of eclipse PDE")

    val copyPlatformJars = taskKey[Unit]("retrieves the platform jars from the eclipse workspace PDE pool into the unmanaged jars directory")
    val testtask = taskKey[Unit]("TestTask")
    val jctApi = settingKey[JCTApi]("the local jct API")
    val pdedevApi = settingKey[PDEApi]("the RCP dev api")
    val pdedevProduct = taskKey[JCTProduct]("the jct product from the pdedev workspace")
  }
  import Keys._

  // val swtSettings = Seq(
  //   )
  val defaultSettings = Seq(
      jctjars_jctplatform_loc := baseDirectory.value / "jctjars" / "jctplatform",
      jctjars_swtplus_loc := baseDirectory.value/ "jctjars" / "swtplus",
      pdedevApi := {
        new PDEApi(jct_pde_gitbase.value, jct_pde_workspace.value)
      },
      jctjars_swtplus_list := {
        val finder: PathFinder = jctjars_swtplus_loc.value ** "*.jar"
        finder.get map Attributed.blank
      },
      jctjars_jctplatform_list := {
        val finder: PathFinder = jctjars_jctplatform_loc.value ** "*.jar"
        finder.get map Attributed.blank
      },
      jctApi := {
        val basedir = baseDirectory.value
        new JCTApi(Path(basedir))
      },
      pdedevProduct := {
        pdedevApi.value.productReusing
      },
      rcpClearData := {
        os.remove.all(jct_pde_datadir.value)
      },
      rcpRun := {
        pdedevApi.value.runcmd(
          jct_pde_runconfig.value,
          jct_pde_datadir.value
        ).connectOut.printdbg.call()
      },
      testtask := {
        println("testtask run!")
      },
      copyPlatformJars := {
        val pde = pdedevApi.value
        val product = pde.productReusing
        val swtplus = JCTJars.findSourceAnnotatedSWTPlusJars( product, pde.poolJars.path )
        val swtplusWithSource = swtplus.filter(_.source.isDefined)
        println(s"Jars found with source attachment: ${swtplusWithSource.map{_.artifact.id}.mkString("\n")}")
        val jctplatform = JCTJars.findSourceAnnotatedJCTPlatformJars(product, pde.poolJars.path)
        val jctplatformWithSource = swtplus.filter(_.source.isDefined)
        println(s"Jars found with source attachment: ${jctplatformWithSource.map{_.artifact.id}.mkString("\n")}")
        JCTJars.copyPlatformJarsImpl(
          swtplus,
          Path(baseDirectory.value) / "jctjars" / "swtplus"
        )
        JCTJars.copyPlatformJarsImpl(
          jctplatform,
          Path(baseDirectory.value) / "jctjars" / "jctplatform"
        )
      },
      toRCP := {
        import sbtassembly.AssemblyKeys.assembly
        val assembled = (RCPExport / assembly).value
        val api = pdedevApi.value
        val targetfile = jct_pde_exportjar_path.value
        println(s"targetfile: $targetfile")
        sbt.io.IO.copyFile(assembled, targetfile)
      },
      toRCPAndRun := {
        val toRCPDone = toRCP.value
        pdedevApi.value.runcmd(
          jct_pde_runconfig.value,
          jct_pde_datadir.value
        ).connectOut.printdbg.call()
      }
    )
}

class JCTApi(val projectpath: Path) {
  import JCTApi._

  val wsPath: Path       = projectpath / "jctbuild"
  val pdedevProductPath: Path = wsPath / "pdedevProduct"
  val localwsPath: Path       = wsPath / "ws"
  val datadir: IO[Path]       = PathIO.forDirWorkspace(p => p)(wsPath)(PathIO.reusingPolicy)
  val local: IO[BuildWS]      = localBuildWS.apply(localwsPath)(PathIO.reusingPolicy)
}

object JCTApi {

  def localBuildWS: PathIO[BuildWS] = PathIO.forDirWorkspace(p => new BuildWS {
    override def path: Path = p
  })

  implicit class SbtIOOps[T](io: IO[T]) {
    def asTaskImpl = {
      io.unsafeRunSync()
    }
  }
}
