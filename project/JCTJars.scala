package sbtproj

import os.Path
import libjctbuild.JCTBuild

class JCTJars(val path: Path) {

  import JCTJars._


  def findJars: List[JarPath] = {
    os.exists(path) match {
      case false => List()
      case true => os.walk.apply(path).toList.filter(os.isFile(_)).filter(_.ext == "jar").map(JarPath(_))
    }
  }
  def findPoolRCPJars: List[ArtifactAndSource] = zipSourceWithArtifact(findJars)


  def findSWTPlusJarsFromPool_old: List[ArtifactAndSource] = findPoolRCPJars
    .filter(a => JCTJarsIds.swtplusIds.contains(a.artifact.id))

  def findJCTPlatformJarsFromPool_old: List[ArtifactAndSource] = findPoolRCPJars
    .filter(a => JCTJarsIds.jctplatformIds.contains(a.artifact.id))

  def findSWTPlusJarsFromPool: List[ArtifactAndSource] = findPoolRCPJars
    .filter{ a =>
      a.artifact.id.contains("swt") || a.artifact.id.contains("jface") || a.artifact.id.contains("durian") 
    }
    .filter{! _.artifact.id.contains("win32") }
    .filter{! _.artifact.id.contains("cocoa") }
    .filter{! _.artifact.id.contains("e4") }

  def findJCTPlatformJarsFromPool: List[ArtifactAndSource] = {
    val pooljars = findPoolRCPJars
    pooljars
      .filter{ a =>
        raw"^org.jcryptool.*(util|operation|editor|ui).*".r.findFirstMatchIn(a.artifact.id) match {
          case Some(_) => true
          case None => false
        }
      } ++ pooljars
      .filter{_.artifact.id.startsWith("org.eclipse")}
      // .filter(a => (!a.artifact.id.contains("jface")) && (!a.artifact.id.contains("swt")))
  }

  def zipSourceWithArtifact(jars: List[JarPath]): List[ArtifactAndSource] = {
    val artifacts = jars.filter(! _.isSource)
    val sources = jars.filter(_.isSource)
    def findSourceFor(artifact: JarPath, sourceJarPaths: List[JarPath]) = {
      sourceJarPaths
        // .filter(s => s.path / os.up == artifact.path / os.up)
          .filter{a => a.id == artifact.id}
          .filter{a => a.version == artifact.version}
          .headOption
    }
    artifacts.map(a => ArtifactAndSource(a, findSourceFor(a, sources)))
  }

}

object JCTJars {
  case class ArtifactAndSource(artifact: JarPath, source: Option[JarPath])
  case class JarPath(path: Path) {
    val nameprefix = path.baseName.slice(0,path.last.lastIndexOf("_"))
    val id = nameprefix.replaceAll("\\.source$", "")
    val isSource = nameprefix.endsWith(".source")
    val version = path.baseName.substring(path.last.lastIndexOf("_")+1)
  }

  def findSourceAnnotatedSWTPlusJars(product: JCTBuild.JCTProduct, pdePoolPath: Path) = {
    val jarsFromPoolpath = new JCTJars(pdePoolPath).findJars
    val prod = new JCTJars(product.p2RepositoryPlugins)
    val jars = prod.findSWTPlusJarsFromPool_old
    mergeSourceAttaching(jars, jarsFromPoolpath)
  }
  def findSourceAnnotatedJCTPlatformJars(product: JCTBuild.JCTProduct, pdePoolPath: Path) = {
    val jarsFromPoolpath = new JCTJars(pdePoolPath).findJars
    val prod = new JCTJars(product.p2RepositoryPlugins)
    val jars = prod.findJCTPlatformJarsFromPool_old
    println(s"DBG: ??????  \n${jars.mkString("\n")} ")
    mergeSourceAttaching(jars, jarsFromPoolpath)
  }
  def mergeSourceAttaching(artifacts: List[ArtifactAndSource], input: List[JarPath]): List[ArtifactAndSource] = {
    artifacts.map { case aas@ArtifactAndSource(artifact,s) =>
      s match {
        case Some(_) => aas
        case None => {
          input.filter{_.isSource}.map { srcJar =>
            srcJar.id == artifact.id match {
              case true => Some(ArtifactAndSource(artifact, Some(srcJar)))
              case false => None
            }
          }.flatten.headOption.getOrElse(aas)
        }
      }

    }
  }
  def copyPlatformJarsImpl(
    artifacts: List[ArtifactAndSource], 
    target: Path, 
  ) = {
    artifacts.isEmpty match {
      case true => ()
      case false => {
        os.remove.all(target)
        os.makeDir.all(target)
        artifacts.foreach { a =>
          // println(s"copying ${a}")
          os.copy.into(a.artifact.path, target)
          a.source match {
            case Some(p) => os.copy.into(p.path, target)
            case None => ()
          }
        }
      }
    }
  }
}

object JCTJarsIds {
  val swtplusIds = List(
    "org.eclipse.jface.databinding.nl_de",
    "org.eclipse.jface.databinding",
    "org.eclipse.jface.nl_de",
    "org.eclipse.jface.notifications",
    "org.eclipse.jface.text.nl_de",
    "org.eclipse.jface.text",
    "org.eclipse.jface",
    "org.eclipse.swt",
    "org.eclipse.swt.browser.chromium.gtk.linux.x86_64",
    "org.eclipse.swt.gtk.linux.x86_64",
    )

  val jctplatformIds = List(
    "org.eclipse.compare.core.nl_de",
    "org.eclipse.compare.core",
    "org.eclipse.core.commands.nl_de",
    "org.eclipse.core.commands",
    "org.eclipse.core.contenttype",
    "org.eclipse.core.databinding.beans.nl_de",
    "org.eclipse.core.databinding.beans",
    "org.eclipse.core.databinding.nl_de",
    "org.eclipse.core.databinding.observable.nl_de",
    "org.eclipse.core.databinding.observable",
    "org.eclipse.core.databinding.property.nl_de",
    "org.eclipse.core.databinding.property",
    "org.eclipse.core.databinding",
    "org.eclipse.core.expressions",
    "org.eclipse.core.filebuffers",
    "org.eclipse.core.filesystem",
    "org.eclipse.core.jobs",
    "org.eclipse.core.net.nl_de",
    "org.eclipse.core.net",
    "org.eclipse.core.resources",
    "org.eclipse.core.runtime",
    "org.eclipse.core.variables.nl_de",
    "org.eclipse.core.variables",
    "org.eclipse.debug.core",
    "org.eclipse.draw2d",
    "org.eclipse.e4.core.commands",
    "org.eclipse.e4.core.contexts",
    "org.eclipse.e4.core.di.annotations",
    "org.eclipse.e4.core.di.extensions.supplier",
    "org.eclipse.e4.core.di.extensions",
    "org.eclipse.e4.core.di",
    "org.eclipse.e4.core.services",
    "org.eclipse.e4.emf.xpath",
    "org.eclipse.e4.ui.bindings",
    "org.eclipse.e4.ui.css.core",
    "org.eclipse.e4.ui.di",
    "org.eclipse.e4.ui.dialogs",
    "org.eclipse.e4.ui.ide",
    "org.eclipse.e4.ui.model.workbench",
    "org.eclipse.e4.ui.services",
    "org.eclipse.e4.ui.widgets",
    "org.eclipse.e4.ui.workbench3",
    "org.eclipse.e4.ui.workbench",
    "org.eclipse.ecf.filetransfer",
    "org.eclipse.ecf.identity",
    "org.eclipse.ecf.provider.filetransfer.httpclient45.win32",
    "org.eclipse.ecf.provider.filetransfer.httpclient45",
    "org.eclipse.ecf.provider.filetransfer.ssl",
    "org.eclipse.ecf.provider.filetransfer",
    "org.eclipse.ecf.ssl",
    "org.eclipse.ecf",
    "org.eclipse.emf.common",
    "org.eclipse.emf.ecore.change",
    "org.eclipse.emf.ecore.xmi",
    "org.eclipse.emf.ecore",
    "org.eclipse.equinox.app",
    "org.eclipse.equinox.bidi",
    "org.eclipse.equinox.common",
    "org.eclipse.equinox.concurrent",
    "org.eclipse.equinox.console",
    "org.eclipse.equinox.event",
    "org.eclipse.equinox.frameworkadmin.equinox",
    "org.eclipse.equinox.frameworkadmin",
    "org.eclipse.equinox.http.jetty",
    "org.eclipse.equinox.http.registry",
    "org.eclipse.equinox.http.servlet",
    "org.eclipse.equinox.jsp.jasper.registry",
    "org.eclipse.equinox.jsp.jasper",
    "org.eclipse.equinox.launcher.cocoa.macosx.x86_64",
    "org.eclipse.equinox.launcher.gtk.linux.x86_64",
    "org.eclipse.equinox.launcher.nl_de",
    "org.eclipse.equinox.launcher.win32.win32.x86_64",
    "org.eclipse.equinox.launcher",
    "org.eclipse.equinox.p2.artifact.repository",
    "org.eclipse.equinox.p2.console",
    "org.eclipse.equinox.p2.core",
    "org.eclipse.equinox.p2.director.app",
    "org.eclipse.equinox.p2.director",
    "org.eclipse.equinox.p2.directorywatcher",
    "org.eclipse.equinox.p2.engine",
    "org.eclipse.equinox.p2.extensionlocation",
    "org.eclipse.equinox.p2.garbagecollector",
    "org.eclipse.equinox.p2.jarprocessor",
    "org.eclipse.equinox.p2.metadata.repository",
    "org.eclipse.equinox.p2.metadata",
    "org.eclipse.equinox.p2.operations",
    "org.eclipse.equinox.p2.publisher.eclipse",
    "org.eclipse.equinox.p2.publisher",
    "org.eclipse.equinox.p2.reconciler.dropins",
    "org.eclipse.equinox.p2.repository.tools",
    "org.eclipse.equinox.p2.repository",
    "org.eclipse.equinox.p2.touchpoint.eclipse",
    "org.eclipse.equinox.p2.touchpoint.natives",
    "org.eclipse.equinox.p2.transport.ecf",
    "org.eclipse.equinox.p2.ui.sdk.scheduler",
    "org.eclipse.equinox.p2.ui.sdk",
    "org.eclipse.equinox.p2.ui",
    "org.eclipse.equinox.p2.updatechecker",
    "org.eclipse.equinox.preferences",
    "org.eclipse.equinox.registry",
    "org.eclipse.equinox.security.linux.x86_64",
    "org.eclipse.equinox.security.macosx",
    "org.eclipse.equinox.security.ui",
    "org.eclipse.equinox.security.win32.x86_64",
    "org.eclipse.equinox.security",
    "org.eclipse.equinox.simpleconfigurator.manipulator",
    "org.eclipse.equinox.simpleconfigurator",
    "org.eclipse.gef",
    "org.eclipse.help.base.nl_de",
    "org.eclipse.help.base",
    "org.eclipse.help.nl_de",
    "org.eclipse.help.ui.nl_de",
    "org.eclipse.help.ui",
    "org.eclipse.help.webapp.nl_de",
    "org.eclipse.help.webapp",
    "org.eclipse.help",
    "org.eclipse.jetty.continuation",
    "org.eclipse.jetty.http",
    "org.eclipse.jetty.io",
    "org.eclipse.jetty.security",
    "org.eclipse.jetty.server",
    "org.eclipse.jetty.servlet",
    "org.eclipse.jetty.util",
    "org.eclipse.osgi.compatibility.state",
    "org.eclipse.osgi.nl_de",
    "org.eclipse.osgi.services.nl_de",
    "org.eclipse.osgi.services",
    "org.eclipse.osgi.util",
    "org.eclipse.osgi",
    "org.eclipse.rcp",
    "org.eclipse.text.nl_de",
    "org.eclipse.text",
    "org.eclipse.ui.browser.nl_de",
    "org.eclipse.ui.browser",
    "org.eclipse.ui.cocoa",
    "org.eclipse.ui.console.nl_de",
    "org.eclipse.ui.console",
    "org.eclipse.ui.editors",
    "org.eclipse.ui.forms.nl_de",
    "org.eclipse.ui.forms",
    "org.eclipse.ui.ide",
    "org.eclipse.ui.intro.nl_de",
    "org.eclipse.ui.intro",
    "org.eclipse.ui.navigator",
    "org.eclipse.ui.net.nl_de",
    "org.eclipse.ui.net",
    "org.eclipse.ui.nl_de",
    "org.eclipse.ui.views",
    "org.eclipse.ui.workbench.nl_de",
    "org.eclipse.ui.workbench.texteditor.nl_de",
    "org.eclipse.ui.workbench.texteditor",
    "org.eclipse.ui.workbench",
    "org.eclipse.ui",
    "org.eclipse.update.configurator.nl_de",
    "org.eclipse.update.configurator",
    "org.eclipse.urischeme",
    "org.eclipse.zest.core",
    "org.eclipse.zest.layouts",
    "org.jcryptool.actions.ui",
    "org.jcryptool.commands.ui",
    "org.jcryptool.core.operations",
    "org.jcryptool.core.util",
    "org.jcryptool.core.help",
    "org.jcryptool.crypto.flexiprovider.operations",
    "org.jcryptool.crypto.ui",
    "org.jcryptool.editor.hex",
    "org.jcryptool.editor.text",
    )
}

