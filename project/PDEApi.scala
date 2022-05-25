package sbtproj

import libjctbuild.JCTBuild._
import libjctbuild.SimleiGit._
import os.Path
import libjctbuild.Util._
import libjctbuild.SimleiGithub
import cats.effect._
import cats.effect.unsafe.implicits.global
import sbt.{IO => sbtIO, _}
import sbt.Keys._

// for managing the RCP PDE  // TODO: Rename PDEDev
class PDEApi(val gitBase: Path, val workspacePath: Path) {
  def runcmd(runconfig: String, datadir: Path) = Cmd.of(PDEApi.runcmd(workspacePath, runconfig, datadir))
  val poolPath = workspacePath / os.RelPath(".metadata/.plugins/org.eclipse.pde.core/.bundle_pool/plugins/")
  def poolJars = new JCTJars(poolPath)
  def productJarsReusing = new JCTJars(productReusing.p2RepositoryPlugins)
  def productReusing: JCTProduct = buildWS.flatMap{_.productReusing}.unsafeRunSync()
  def buildWS = PathIO.forDirWorkspace(p => new BuildWS {
    override def path: Path = p
  })(gitBase)(PathIO.reusingPolicy)

  def compileRoutine: IO[JCTProduct] = for {
    app <- buildWS
    product <- app.build(WeeklyBuild)
  } yield product
}

object PDEApi {

  def runcmd_raw(workspace: Path, runconfig: String, datadir: Path) = {
s"""
/home/snuc/.sdkman/candidates/java/16.0.1.hs-adpt/bin/java
-Xms40m
-Xmx512m
-Dorg.eclipse.swt.graphics.Resource.reportNonDisposed=true
-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog
--add-modules=ALL-SYSTEM
-Djceks.key.serialFilter=*
-Dorg.bouncycastle.rsa.allow_unsafe_mod=true
-Dfile.encoding=UTF-8
-classpath $workspace/.metadata/.plugins/org.eclipse.pde.core/.bundle_pool/plugins/org.eclipse.equinox.launcher_1.5.800.v20200727-1323.jar
-XX:+ShowCodeDetailsInExceptionMessages org.eclipse.equinox.launcher.Main
-launcher $workspace/.metadata/.plugins/org.eclipse.pde.core/.bundle_pool/eclipse
-name Eclipse
-showsplash 600
-product org.jcryptool.core.product
-data $datadir
-configuration file:$workspace/.metadata/.plugins/org.eclipse.pde.core/$runconfig/
-dev file:/home/snuc/git/JCT_bciws/bcidevWS/.metadata/.plugins/org.eclipse.pde.core/$runconfig/dev.properties
-os linux
-ws gtk
-arch x86_64
-nl en_US
-consoleLog
"""
  }

  
  def runcmd(workspace: Path, runconfig: String, datadir: Path): List[String] = 
    runcmd_raw(workspace, runconfig, datadir).linesIterator.flatMap[String](s => s.split(" ").toStream).filter(! _.isEmpty()).toList

}
